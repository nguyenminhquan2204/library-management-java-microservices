package com.javamicroservices.borrowingservice.command.aggregate;

import java.math.BigDecimal;
import java.util.Date;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import com.javamicroservices.borrowingservice.command.command.CancelBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.ConfirmBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.CreateBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.FailBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.NotifyBorrowingDueSoonCommand;
import com.javamicroservices.borrowingservice.command.command.RecordBorrowingOverdueCommand;
import com.javamicroservices.borrowingservice.command.command.ReturnBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.UpdateBorrowingCommand;
import com.javamicroservices.borrowingservice.command.data.BorrowingStatus;
import com.javamicroservices.borrowingservice.command.event.BorrowingCancelledEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingConfirmedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingCreatedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingDueSoonNotifiedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingFailedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingOverdueRecordedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingReturnedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingUpdatedEvent;
import com.javamicroservices.borrowingservice.command.model.BorrowingUpdateResponse;
import com.javamicroservices.borrowingservice.configuration.BorrowingPolicy;
import com.javamicroservices.commonservice.exception.BadRequestException;
import com.javamicroservices.commonservice.exception.ConflictException;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Aggregate 
public class BorrowingAggregate {
    @AggregateIdentifier 
    private String id;

    private String bookId;

    // Bản sao được giữ cho phiếu mượn, chỉ có sau khi CONFIRMED
    private String bookCopyId;

    private String employeeId;

    private BorrowingStatus status;

    private Date borrowingDate;

    private Date dueDate;

    private Date returnDate;

    private BigDecimal fineAmount = BigDecimal.ZERO;

    private boolean dueSoonNotified;

    private Date lastOverdueNotifiedAt;

    public BorrowingAggregate() {}

    @CommandHandler 
    public BorrowingAggregate(CreateBorrowingCommand command) {
        BorrowingCreatedEvent event = new BorrowingCreatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    /**
     * Saga đã giữ được bản sao -> xác nhận phiếu mượn. Nếu phiếu đã bị huỷ (timeout) thì từ chối để saga release bản sao.
     */
    @CommandHandler
    public void handle(ConfirmBorrowingCommand command) {
        requireStatus(BorrowingStatus.PENDING, "confirm");
        AggregateLifecycle.apply(new BorrowingConfirmedEvent(this.id, this.bookId, command.getBookCopyId(), this.employeeId, command.getReservationId()));
    }

    @CommandHandler
    public void handle(FailBorrowingCommand command) {
        requireStatus(BorrowingStatus.PENDING, "fail");
        AggregateLifecycle.apply(new BorrowingFailedEvent(this.id, command.getReason()));
    }

    @CommandHandler
    public void handle(CancelBorrowingCommand command) {
        requireStatus(BorrowingStatus.PENDING, "cancel");
        AggregateLifecycle.apply(new BorrowingCancelledEvent(this.id, command.getReason()));
    }

    @CommandHandler 
    public void handle(ReturnBorrowingCommand command, BorrowingPolicy policy) {
        if (this.status == BorrowingStatus.RETURNED) {
            throw new ConflictException("Borrowing with bookId " + this.bookId + " already returned in " + this.returnDate);
        }
        requireStatus(BorrowingStatus.CONFIRMED, "return");
        if (!this.bookId.equals(command.getBookId()) || !this.employeeId.equals(command.getEmployeeId())) {
            throw new BadRequestException("BookId or employeeId does not match this borrowing");
        }

        BorrowingReturnedEvent event = new BorrowingReturnedEvent();
        BeanUtils.copyProperties(command, event);
        event.setBookCopyId(this.bookCopyId);
        // Chốt tiền phạt tại thời điểm trả sách
        event.setFineAmount(policy.calculateFine(this.dueDate, command.getReturnDate()));
        AggregateLifecycle.apply(event);
    }

    @CommandHandler
    public BorrowingUpdateResponse handle(UpdateBorrowingCommand command) {
        Date borrowingDate = command.getBorrowingDate() != null ? command.getBorrowingDate() : null;
        Date returnDate = command.getReturnDate() != null ? command.getReturnDate() : null;
        // Không truyền dueDate thì giữ nguyên hạn trả cũ
        Date dueDate = command.getDueDate() != null ? command.getDueDate() : this.dueDate;

        // Bản sao gắn với đầu sách ban đầu, đổi sách phải tạo phiếu mượn mới
        if (command.getBookId() != null && !command.getBookId().equals(this.bookId)) {
            throw new BadRequestException("Cannot change bookId of a borrowing");
        }
        // Trả sách phải đi qua API trả để saga release bản sao
        if (returnDate != null && this.status != BorrowingStatus.RETURNED) {
            throw new BadRequestException("Use the return API to return a book");
        }
        if (returnDate != null && returnDate.before(borrowingDate)) {
            throw new BadRequestException("Return date must be after borrowing date");
        }
        if (dueDate != null && borrowingDate != null && dueDate.before(borrowingDate)) {
            throw new BadRequestException("Due date must be after borrowing date");
        }

        BorrowingUpdatedEvent event = new BorrowingUpdatedEvent(command.getId(), command.getBookId(), command.getEmployeeId(), borrowingDate, returnDate, dueDate);
        AggregateLifecycle.apply(event);

        return new BorrowingUpdateResponse(this.id, this.bookId, this.employeeId, this.borrowingDate, this.returnDate, this.dueDate);
    }

    @CommandHandler
    public void handle(NotifyBorrowingDueSoonCommand command) {
        requireStatus(BorrowingStatus.CONFIRMED, "notify due soon for");
        if (this.dueSoonNotified) {
            throw new ConflictException("Due soon reminder already sent for borrowing " + this.id);
        }

        AggregateLifecycle.apply(new BorrowingDueSoonNotifiedEvent(command.getId(), command.getNotifiedAt()));
    }

    /**
     * Ghi nhận phiếu mượn đang quá hạn và tiền phạt tạm tính tới thời điểm kiểm tra.
     * Mỗi ngày chỉ ghi nhận 1 lần, nên cronjob chạy lại (hoặc chạy trên nhiều instance) cũng không gửi email trùng.
     */
    @CommandHandler
    public BorrowingOverdueRecordedEvent handle(RecordBorrowingOverdueCommand command, BorrowingPolicy policy) {
        requireStatus(BorrowingStatus.CONFIRMED, "record overdue for");
        long overdueDays = policy.overdueDays(this.dueDate, command.getCheckedAt());
        if (overdueDays <= 0) {
            throw new BadRequestException("Borrowing " + this.id + " is not overdue");
        }
        if (this.lastOverdueNotifiedAt != null && policy.isSameDay(this.lastOverdueNotifiedAt, command.getCheckedAt())) {
            throw new ConflictException("Overdue already recorded today for borrowing " + this.id);
        }

        BorrowingOverdueRecordedEvent event = new BorrowingOverdueRecordedEvent(
            command.getId(), overdueDays, policy.calculateFine(this.dueDate, command.getCheckedAt()), command.getCheckedAt());
        AggregateLifecycle.apply(event);
        return event;
    }

    private void requireStatus(BorrowingStatus expected, String action) {
        if (this.status != expected) {
            throw new ConflictException("Cannot " + action + " borrowing " + this.id + " with status " + this.status);
        }
    }

    @EventSourcingHandler
    public void on(BorrowingCreatedEvent event) {
        this.id = event.getId();
        this.bookId = event.getBookId();
        this.employeeId = event.getEmployeeId();
        this.borrowingDate = event.getBorrowingDate();
        this.dueDate = event.getDueDate();
        this.status = BorrowingStatus.PENDING;
    }

    @EventSourcingHandler
    public void on(BorrowingConfirmedEvent event) {
        this.bookCopyId = event.getBookCopyId();
        this.status = BorrowingStatus.CONFIRMED;
    }

    @EventSourcingHandler
    public void on(BorrowingFailedEvent event) {
        this.status = BorrowingStatus.FAILED;
    }

    @EventSourcingHandler
    public void on(BorrowingCancelledEvent event) {
        this.status = BorrowingStatus.CANCELLED;
    }

    @EventSourcingHandler
    public void on(BorrowingUpdatedEvent event) {
        this.id = event.getId();
        this.bookId = event.getBookId();
        this.employeeId = event.getEmployeeId();
        this.borrowingDate = event.getBorrowingDate();
        this.returnDate = event.getReturnDate();
        if (event.getDueDate() != null && !event.getDueDate().equals(this.dueDate)) {
            // Đổi hạn trả (gia hạn) -> cho phép nhắc lại với hạn mới
            this.dueSoonNotified = false;
        }
        this.dueDate = event.getDueDate();
    }

    @EventSourcingHandler
    public void on(BorrowingReturnedEvent event) {
        this.status = BorrowingStatus.RETURNED;
        this.returnDate = event.getReturnDate();
        if (event.getFineAmount() != null) {
            this.fineAmount = event.getFineAmount();
        }
    }

    @EventSourcingHandler
    public void on(BorrowingDueSoonNotifiedEvent event) {
        this.dueSoonNotified = true;
    }

    @EventSourcingHandler
    public void on(BorrowingOverdueRecordedEvent event) {
        this.fineAmount = event.getFineAmount();
        this.lastOverdueNotifiedAt = event.getRecordedAt();
    }
}
