package com.javamicroservices.borrowingservice.command.saga;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import com.javamicroservices.borrowingservice.command.command.CancelBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.ConfirmBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.FailBorrowingCommand;
import com.javamicroservices.borrowingservice.command.event.BorrowingCancelledEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingConfirmedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingCreatedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingFailedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingReturnedEvent;
import com.javamicroservices.borrowingservice.configuration.BorrowingPolicy;
import com.javamicroservices.commonservice.command.ConfirmBookCopyBorrowedCommand;
import com.javamicroservices.commonservice.command.ReleaseBookCopyCommand;
import com.javamicroservices.commonservice.command.ReserveBookCopyCommand;
import com.javamicroservices.commonservice.event.BookCopyReservationFailedEvent;
import com.javamicroservices.commonservice.event.BookCopyReservedEvent;
import com.javamicroservices.commonservice.model.EmployeeResponseCommonModel;
import com.javamicroservices.commonservice.queries.GetDetailEmployeeQuery;

import lombok.extern.slf4j.Slf4j;

/**
 * Mượn sách:
 *   BorrowingCreated (PENDING) -> kiểm tra nhân viên -> ReserveBookCopy
 *     -> BookCopyReservationFailed -> FailBorrowing (FAILED)
 *     -> BookCopyReserved -> ConfirmBorrowing (CONFIRMED) -> ConfirmBookCopyBorrowed (copy BORROWED)
 *                              \-> lỗi -> ReleaseBookCopy (compensation) + FailBorrowing
 *   Quá pending-timeout mà chưa CONFIRMED -> CancelBorrowing (CANCELLED), đã giữ bản sao thì release.
 *
 * Trả sách:
 *   BorrowingReturned -> ReleaseBookCopy đúng bookCopyId của phiếu mượn.
 */
@Slf4j
@Saga
public class BorrowingSaga {
    private static final String PENDING_TIMEOUT = "borrowing-pending-timeout";

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @Autowired
    private transient DeadlineManager deadlineManager;

    @Autowired
    private transient BorrowingPolicy borrowingPolicy;

    private String borrowingId;

    private String bookId;

    private String bookCopyId;

    // Đã gửi ReserveBookCopyCommand nhưng chưa nhận event kết quả: chưa được kết thúc saga,
    // nếu không bản sao giữ muộn (sau khi bị huỷ) sẽ kẹt RESERVED mãi
    private boolean awaitingReservation;

    private boolean cancelled;

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    private void handle(BorrowingCreatedEvent event) {
        log.info("BorrowingCreatedEvent in saga for BookId: " + event.getBookId() + " : EmployeeId: " + event.getEmployeeId());
        this.borrowingId = event.getId();
        this.bookId = event.getBookId();
        deadlineManager.schedule(borrowingPolicy.getPendingTimeout(), PENDING_TIMEOUT);

        try {
            GetDetailEmployeeQuery query = new GetDetailEmployeeQuery(event.getEmployeeId());
            EmployeeResponseCommonModel employeeModel = queryGateway.query(query, ResponseTypes.instanceOf(EmployeeResponseCommonModel.class)).join();
            if (Boolean.TRUE.equals(employeeModel.getIsDisciplined())) {
                failBorrowing("This employee is blocked");
                return;
            }
        } catch (Exception e) {
            failBorrowing("Cannot verify employee: " + e.getMessage());
            return;
        }

        this.awaitingReservation = true;
        try {
            commandGateway.sendAndWait(new ReserveBookCopyCommand(bookId, UUID.randomUUID().toString(), borrowingId));
        } catch (Exception e) {
            // Sách không tồn tại / đã bị xoá
            this.awaitingReservation = false;
            failBorrowing("Cannot reserve book: " + e.getMessage());
        }
    }

    // Liên kết theo borrowingId (key "id" của saga), không theo bookId: nhiều saga cùng một cuốn sách sẽ không nhận nhầm event của nhau
    @SagaEventHandler(associationProperty = "borrowingId", keyName = "id")
    private void handle(BookCopyReservedEvent event) {
        log.info("BookCopyReservedEvent in saga for borrowingId {}: copy {}", event.getBorrowingId(), event.getBookCopyId());
        this.awaitingReservation = false;
        this.bookCopyId = event.getBookCopyId();

        if (cancelled) {
            releaseCopy();
            endSaga();
            return;
        }

        try {
            commandGateway.sendAndWait(new ConfirmBorrowingCommand(borrowingId, bookCopyId, event.getReservationId()));
        } catch (Exception e) {
            log.error("Confirm borrowing {} failed, releasing copy {}: {}", borrowingId, bookCopyId, e.getMessage());
            releaseCopy();
            failBorrowing("Cannot confirm borrowing: " + e.getMessage());
        }
    }

    @SagaEventHandler(associationProperty = "borrowingId", keyName = "id")
    private void handle(BookCopyReservationFailedEvent event) {
        log.info("BookCopyReservationFailedEvent in saga for borrowingId {}: {}", event.getBorrowingId(), event.getReason());
        this.awaitingReservation = false;
        if (cancelled) {
            endSaga();
            return;
        }
        failBorrowing(event.getReason());
    }

    @SagaEventHandler(associationProperty = "id")
    private void handle(BorrowingConfirmedEvent event) {
        log.info("BorrowingConfirmedEvent in saga for borrowingId {}", event.getId());
        try {
            commandGateway.sendAndWait(new ConfirmBookCopyBorrowedCommand(event.getBookId(), event.getBookCopyId(), event.getId()));
            log.info("Borrowing book is successfully!");
        } catch (Exception e) {
            // Phiếu mượn đã CONFIRMED nên không release. Bản sao vẫn RESERVED cho đúng borrowing này,
            // không ai khác lấy được và vẫn release được khi trả sách.
            log.error("Cannot mark copy {} as borrowed for borrowing {}: {}", event.getBookCopyId(), event.getId(), e.getMessage());
        } finally {
            endSaga();
        }
    }

    @SagaEventHandler(associationProperty = "id")
    private void handle(BorrowingFailedEvent event) {
        log.info("BorrowingFailedEvent in saga for borrowingId {}: {}", event.getId(), event.getReason());
        endSaga();
    }

    @SagaEventHandler(associationProperty = "id")
    private void handle(BorrowingCancelledEvent event) {
        log.info("BorrowingCancelledEvent in saga for borrowingId {}: {}", event.getId(), event.getReason());
        this.cancelled = true;
        if (bookCopyId != null) {
            releaseCopy();
            endSaga();
        } else if (!awaitingReservation) {
            endSaga();
        }
        // awaitingReservation: chờ event giữ bản sao về rồi mới release / kết thúc
    }

    @DeadlineHandler(deadlineName = PENDING_TIMEOUT)
    private void onPendingTimeout() {
        log.warn("Borrowing {} still PENDING after {}, cancelling", borrowingId, borrowingPolicy.getPendingTimeout());
        try {
            commandGateway.sendAndWait(new CancelBorrowingCommand(borrowingId, "Borrowing timed out after " + borrowingPolicy.getPendingTimeout()));
        } catch (Exception e) {
            // Phiếu đã CONFIRMED / FAILED ngay trước khi hết hạn
            log.info("Skip cancelling borrowing {}: {}", borrowingId, e.getMessage());
        }
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    private void handle(BorrowingReturnedEvent event) {
        log.info("BorrowingReturnedEvent in saga for BookId: " + event.getBookId() + " : EmployeeId: " + event.getEmployeeId());

        try {
            commandGateway.sendAndWait(new ReleaseBookCopyCommand(event.getBookId(), event.getBookCopyId(), event.getId()));
        } catch (Exception e) {
            log.error("Cannot release copy {} for borrowing {}: {}", event.getBookCopyId(), event.getId(), e.getMessage());
        } finally {
            SagaLifecycle.end();
        }
    }

    private void failBorrowing(String reason) {
        try {
            commandGateway.sendAndWait(new FailBorrowingCommand(borrowingId, reason));
        } catch (Exception e) {
            // Phiếu không còn PENDING (đã bị huỷ) -> không có BorrowingFailedEvent để kết thúc saga
            log.error("Cannot fail borrowing {}: {}", borrowingId, e.getMessage());
            endSaga();
        }
    }

    private void releaseCopy() {
        try {
            commandGateway.sendAndWait(new ReleaseBookCopyCommand(bookId, bookCopyId, borrowingId));
        } catch (Exception e) {
            log.error("Cannot release copy {} for borrowing {}: {}", bookCopyId, borrowingId, e.getMessage());
        }
    }

    private void endSaga() {
        deadlineManager.cancelAllWithinScope(PENDING_TIMEOUT);
        SagaLifecycle.end();
    }
}
