package com.javamicroservices.bookservice.command.aggregate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import com.javamicroservices.bookservice.command.command.AddBookCopyCommand;
import com.javamicroservices.bookservice.command.command.CreateBookCommand;
import com.javamicroservices.bookservice.command.command.DeleteBookCommand;
import com.javamicroservices.bookservice.command.command.MarkBookCopyDamagedCommand;
import com.javamicroservices.bookservice.command.command.MarkBookCopyLostCommand;
import com.javamicroservices.bookservice.command.command.RemoveBookCopyCommand;
import com.javamicroservices.bookservice.command.command.UpdateBookCommand;
import com.javamicroservices.bookservice.command.data.BookCopyStatus;
import com.javamicroservices.bookservice.command.event.BookCopyAddedEvent;
import com.javamicroservices.bookservice.command.event.BookCopyMarkedDamagedEvent;
import com.javamicroservices.bookservice.command.event.BookCopyMarkedLostEvent;
import com.javamicroservices.bookservice.command.event.BookCopyRemovedEvent;
import com.javamicroservices.bookservice.command.event.BookCreatedEvent;
import com.javamicroservices.bookservice.command.event.BookDeletedEvent;
import com.javamicroservices.bookservice.command.event.BookUpdatedEvent;
import com.javamicroservices.commonservice.command.ConfirmBookCopyBorrowedCommand;
import com.javamicroservices.commonservice.command.ReleaseBookCopyCommand;
import com.javamicroservices.commonservice.command.ReserveBookCopyCommand;
import com.javamicroservices.commonservice.event.BookCopyBorrowedEvent;
import com.javamicroservices.commonservice.event.BookCopyReleasedEvent;
import com.javamicroservices.commonservice.event.BookCopyReservationFailedEvent;
import com.javamicroservices.commonservice.event.BookCopyReservedEvent;
import com.javamicroservices.commonservice.exception.ConflictException;
import com.javamicroservices.commonservice.exception.NotFoundException;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Consistency boundary của 1 đầu sách và toàn bộ bản sao của nó.
 * Mọi command cùng bookId được Axon xử lý tuần tự (khoá theo aggregate + kiểm tra sequence number khi append event),
 * nên 2 request tranh bản sao cuối cùng chỉ có 1 request giữ được.
 */
@Aggregate
@NoArgsConstructor
public class BookAggregate {

    @AggregateIdentifier
    private String id;

    private String name;

    private String author;

    // LinkedHashMap: luôn chọn bản sao theo thứ tự được thêm vào
    private Map<String, CopyState> copies = new LinkedHashMap<>();

    // equals: AggregateTestFixture so sánh state sau khi source lại event
    @EqualsAndHashCode
    private static class CopyState {
        private BookCopyStatus status;

        private String barcode;

        // Borrowing đang giữ bản sao khi RESERVED / BORROWED
        private String borrowingId;

        private CopyState(BookCopyStatus status, String barcode) {
            this.status = status;
            this.barcode = barcode;
        }

        private boolean isHeldBy(String borrowingId) {
            return (status == BookCopyStatus.RESERVED || status == BookCopyStatus.BORROWED)
                && borrowingId != null && borrowingId.equals(this.borrowingId);
        }
    }

    @CommandHandler
    public BookAggregate(CreateBookCommand command) {
        AggregateLifecycle.apply(new BookCreatedEvent(command.getId(), command.getName(), command.getAuthor()));
        for (int i = 0; i < command.getInitialCopies(); i++) {
            AggregateLifecycle.apply(new BookCopyAddedEvent(command.getId(), UUID.randomUUID().toString(), null, null, null));
        }
    }

    @CommandHandler
    public void handle(UpdateBookCommand command) {
        AggregateLifecycle.apply(new BookUpdatedEvent(command.getId(), command.getName(), command.getAuthor()));
    }

    @CommandHandler
    public void handle(DeleteBookCommand command) {
        boolean inUse = copies.values().stream()
            .anyMatch(copy -> copy.status == BookCopyStatus.RESERVED || copy.status == BookCopyStatus.BORROWED);
        if (inUse) {
            throw new ConflictException("Cannot delete book " + this.id + " while some copies are reserved or borrowed");
        }
        AggregateLifecycle.apply(new BookDeletedEvent(command.getId()));
    }

    @CommandHandler
    public String handle(AddBookCopyCommand command) {
        String barcode = command.getBarcode();
        if (barcode != null && copies.values().stream().anyMatch(copy -> barcode.equals(copy.barcode))) {
            throw new ConflictException("Barcode " + barcode + " already exists in book " + this.id);
        }
        AggregateLifecycle.apply(new BookCopyAddedEvent(this.id, command.getBookCopyId(), barcode, command.getLocation(), command.getCondition()));
        return command.getBookCopyId();
    }

    @CommandHandler
    public void handle(RemoveBookCopyCommand command) {
        CopyState copy = requireCopy(command.getBookCopyId());
        if (copy.status == BookCopyStatus.RESERVED || copy.status == BookCopyStatus.BORROWED) {
            throw new ConflictException("Cannot remove book copy " + command.getBookCopyId() + " while it is " + copy.status);
        }
        AggregateLifecycle.apply(new BookCopyRemovedEvent(this.id, command.getBookCopyId()));
    }

    @CommandHandler
    public void handle(MarkBookCopyLostCommand command) {
        requireAvailable(command.getBookCopyId());
        AggregateLifecycle.apply(new BookCopyMarkedLostEvent(this.id, command.getBookCopyId()));
    }

    @CommandHandler
    public void handle(MarkBookCopyDamagedCommand command) {
        requireAvailable(command.getBookCopyId());
        AggregateLifecycle.apply(new BookCopyMarkedDamagedEvent(this.id, command.getBookCopyId()));
    }

    /**
     * Không ném exception khi hết sách: saga cần nhận được event thất bại để chuyển borrowing sang FAILED.
     */
    @CommandHandler
    public void handle(ReserveBookCopyCommand command) {
        // Saga gửi lại cùng borrowing -> không giữ thêm bản thứ 2
        if (copies.values().stream().anyMatch(copy -> copy.isHeldBy(command.getBorrowingId()))) {
            return;
        }

        copies.entrySet().stream()
            .filter(entry -> entry.getValue().status == BookCopyStatus.AVAILABLE)
            .map(Map.Entry::getKey)
            .findFirst()
            .ifPresentOrElse(
                copyId -> AggregateLifecycle.apply(new BookCopyReservedEvent(this.id, copyId, command.getReservationId(), command.getBorrowingId())),
                () -> AggregateLifecycle.apply(new BookCopyReservationFailedEvent(this.id, command.getReservationId(), command.getBorrowingId(),
                    "No available copy for book " + this.name))
            );
    }

    @CommandHandler
    public void handle(ConfirmBookCopyBorrowedCommand command) {
        CopyState copy = requireCopy(command.getBookCopyId());
        if (copy.status == BookCopyStatus.BORROWED && copy.isHeldBy(command.getBorrowingId())) {
            return;
        }
        if (copy.status != BookCopyStatus.RESERVED || !copy.isHeldBy(command.getBorrowingId())) {
            throw new ConflictException("Book copy " + command.getBookCopyId() + " is not reserved for borrowing " + command.getBorrowingId());
        }
        AggregateLifecycle.apply(new BookCopyBorrowedEvent(this.id, command.getBookCopyId(), command.getBorrowingId()));
    }

    /**
     * Chỉ release đúng bản sao mà borrowing đang giữ, không lấy ngẫu nhiên bản khác của cùng đầu sách.
     */
    @CommandHandler
    public void handle(ReleaseBookCopyCommand command) {
        CopyState copy = requireCopy(command.getBookCopyId());
        if (!copy.isHeldBy(command.getBorrowingId())) {
            throw new ConflictException("Book copy " + command.getBookCopyId() + " is not held by borrowing " + command.getBorrowingId());
        }
        AggregateLifecycle.apply(new BookCopyReleasedEvent(this.id, command.getBookCopyId(), command.getBorrowingId()));
    }

    private CopyState requireCopy(String bookCopyId) {
        CopyState copy = copies.get(bookCopyId);
        if (copy == null) {
            throw new NotFoundException("Book copy " + bookCopyId + " not found in book " + this.id);
        }
        return copy;
    }

    private void requireAvailable(String bookCopyId) {
        CopyState copy = requireCopy(bookCopyId);
        if (copy.status != BookCopyStatus.AVAILABLE) {
            throw new ConflictException("Book copy " + bookCopyId + " is " + copy.status + ", only AVAILABLE copy can be changed");
        }
    }

    @EventSourcingHandler
    public void on(BookCreatedEvent event) {
        this.id = event.getId();
        this.name = event.getName();
        this.author = event.getAuthor();
    }

    @EventSourcingHandler
    public void on(BookUpdatedEvent event) {
        this.name = event.getName();
        this.author = event.getAuthor();
    }

    @EventSourcingHandler
    public void on(BookDeletedEvent event) {
        AggregateLifecycle.markDeleted();
    }

    @EventSourcingHandler
    public void on(BookCopyAddedEvent event) {
        copies.put(event.getBookCopyId(), new CopyState(BookCopyStatus.AVAILABLE, event.getBarcode()));
    }

    @EventSourcingHandler
    public void on(BookCopyRemovedEvent event) {
        copies.remove(event.getBookCopyId());
    }

    @EventSourcingHandler
    public void on(BookCopyMarkedLostEvent event) {
        copies.get(event.getBookCopyId()).status = BookCopyStatus.LOST;
    }

    @EventSourcingHandler
    public void on(BookCopyMarkedDamagedEvent event) {
        copies.get(event.getBookCopyId()).status = BookCopyStatus.DAMAGED;
    }

    @EventSourcingHandler
    public void on(BookCopyReservedEvent event) {
        CopyState copy = copies.get(event.getBookCopyId());
        copy.status = BookCopyStatus.RESERVED;
        copy.borrowingId = event.getBorrowingId();
    }

    @EventSourcingHandler
    public void on(BookCopyBorrowedEvent event) {
        copies.get(event.getBookCopyId()).status = BookCopyStatus.BORROWED;
    }

    @EventSourcingHandler
    public void on(BookCopyReleasedEvent event) {
        CopyState copy = copies.get(event.getBookCopyId());
        copy.status = BookCopyStatus.AVAILABLE;
        copy.borrowingId = null;
    }
}
