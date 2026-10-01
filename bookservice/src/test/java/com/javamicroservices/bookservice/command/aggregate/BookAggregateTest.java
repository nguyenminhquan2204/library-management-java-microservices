package com.javamicroservices.bookservice.command.aggregate;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javamicroservices.bookservice.command.command.AddBookCopyCommand;
import com.javamicroservices.bookservice.command.command.DeleteBookCommand;
import com.javamicroservices.bookservice.command.command.MarkBookCopyLostCommand;
import com.javamicroservices.bookservice.command.command.RemoveBookCopyCommand;
import com.javamicroservices.bookservice.command.event.BookCopyAddedEvent;
import com.javamicroservices.bookservice.command.event.BookCopyMarkedLostEvent;
import com.javamicroservices.bookservice.command.event.BookCreatedEvent;
import com.javamicroservices.commonservice.command.ConfirmBookCopyBorrowedCommand;
import com.javamicroservices.commonservice.command.ReleaseBookCopyCommand;
import com.javamicroservices.commonservice.command.ReserveBookCopyCommand;
import com.javamicroservices.commonservice.event.BookCopyBorrowedEvent;
import com.javamicroservices.commonservice.event.BookCopyReleasedEvent;
import com.javamicroservices.commonservice.event.BookCopyReservationFailedEvent;
import com.javamicroservices.commonservice.event.BookCopyReservedEvent;
import com.javamicroservices.commonservice.exception.ConflictException;

class BookAggregateTest {
    private static final String BOOK_ID = "book-1";
    private static final String COPY_1 = "copy-1";
    private static final String COPY_2 = "copy-2";

    private FixtureConfiguration<BookAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(BookAggregate.class);
    }

    private BookCreatedEvent created() {
        return new BookCreatedEvent(BOOK_ID, "Clean Code", "Robert C. Martin");
    }

    private BookCopyAddedEvent copyAdded(String copyId) {
        return new BookCopyAddedEvent(BOOK_ID, copyId, null, null, null);
    }

    private BookCopyReservedEvent reserved(String copyId, String borrowingId) {
        return new BookCopyReservedEvent(BOOK_ID, copyId, "reservation-" + borrowingId, borrowingId);
    }

    private BookCopyBorrowedEvent borrowed(String copyId, String borrowingId) {
        return new BookCopyBorrowedEvent(BOOK_ID, copyId, borrowingId);
    }

    @Test
    void reserveTakesFirstAvailableCopy() {
        fixture.given(created(), copyAdded(COPY_1), copyAdded(COPY_2), reserved(COPY_1, "borrow-1"), borrowed(COPY_1, "borrow-1"))
            .when(new ReserveBookCopyCommand(BOOK_ID, "reservation-borrow-2", "borrow-2"))
            .expectEvents(reserved(COPY_2, "borrow-2"));
    }

    @Test
    void reserveFailsWhenNoCopyAvailable() {
        fixture.given(created(), copyAdded(COPY_1), reserved(COPY_1, "borrow-1"))
            .when(new ReserveBookCopyCommand(BOOK_ID, "reservation-2", "borrow-2"))
            .expectSuccessfulHandlerExecution()
            .expectEvents(new BookCopyReservationFailedEvent(BOOK_ID, "reservation-2", "borrow-2", "No available copy for book Clean Code"));
    }

    @Test
    void lostAndDamagedCopiesAreNeverReserved() {
        fixture.given(created(), copyAdded(COPY_1), new BookCopyMarkedLostEvent(BOOK_ID, COPY_1))
            .when(new ReserveBookCopyCommand(BOOK_ID, "reservation-1", "borrow-1"))
            .expectEvents(new BookCopyReservationFailedEvent(BOOK_ID, "reservation-1", "borrow-1", "No available copy for book Clean Code"));
    }

    @Test
    void reserveAgainForSameBorrowingDoesNotTakeSecondCopy() {
        fixture.given(created(), copyAdded(COPY_1), copyAdded(COPY_2), reserved(COPY_1, "borrow-1"))
            .when(new ReserveBookCopyCommand(BOOK_ID, "reservation-retry", "borrow-1"))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents();
    }

    @Test
    void confirmBorrowedOnlyByReservingBorrowing() {
        fixture.given(created(), copyAdded(COPY_1), reserved(COPY_1, "borrow-1"))
            .when(new ConfirmBookCopyBorrowedCommand(BOOK_ID, COPY_1, "borrow-1"))
            .expectEvents(borrowed(COPY_1, "borrow-1"));

        fixture.given(created(), copyAdded(COPY_1), reserved(COPY_1, "borrow-1"))
            .when(new ConfirmBookCopyBorrowedCommand(BOOK_ID, COPY_1, "borrow-2"))
            .expectException(ConflictException.class);
    }

    @Test
    void releaseReturnsBorrowedCopyToAvailable() {
        fixture.given(created(), copyAdded(COPY_1), reserved(COPY_1, "borrow-1"), borrowed(COPY_1, "borrow-1"))
            .when(new ReleaseBookCopyCommand(BOOK_ID, COPY_1, "borrow-1"))
            .expectEvents(new BookCopyReleasedEvent(BOOK_ID, COPY_1, "borrow-1"));
    }

    @Test
    void releaseReservedCopyAsCompensation() {
        fixture.given(created(), copyAdded(COPY_1), reserved(COPY_1, "borrow-1"))
            .when(new ReleaseBookCopyCommand(BOOK_ID, COPY_1, "borrow-1"))
            .expectEvents(new BookCopyReleasedEvent(BOOK_ID, COPY_1, "borrow-1"));
    }

    @Test
    void releaseRejectsCopyHeldByAnotherBorrowing() {
        fixture.given(created(), copyAdded(COPY_1), copyAdded(COPY_2), reserved(COPY_1, "borrow-1"), reserved(COPY_2, "borrow-2"))
            .when(new ReleaseBookCopyCommand(BOOK_ID, COPY_2, "borrow-1"))
            .expectException(ConflictException.class)
            .expectNoEvents();
    }

    @Test
    void cannotReleaseTwice() {
        fixture.given(created(), copyAdded(COPY_1), reserved(COPY_1, "borrow-1"), new BookCopyReleasedEvent(BOOK_ID, COPY_1, "borrow-1"))
            .when(new ReleaseBookCopyCommand(BOOK_ID, COPY_1, "borrow-1"))
            .expectException(ConflictException.class);
    }

    @Test
    void releasedCopyCanBeReservedAgain() {
        fixture.given(created(), copyAdded(COPY_1), reserved(COPY_1, "borrow-1"), new BookCopyReleasedEvent(BOOK_ID, COPY_1, "borrow-1"))
            .when(new ReserveBookCopyCommand(BOOK_ID, "reservation-borrow-2", "borrow-2"))
            .expectEvents(reserved(COPY_1, "borrow-2"));
    }

    @Test
    void onlyAvailableCopyCanBeMarkedLost() {
        fixture.given(created(), copyAdded(COPY_1))
            .when(new MarkBookCopyLostCommand(BOOK_ID, COPY_1))
            .expectEvents(new BookCopyMarkedLostEvent(BOOK_ID, COPY_1));

        fixture.given(created(), copyAdded(COPY_1), reserved(COPY_1, "borrow-1"))
            .when(new MarkBookCopyLostCommand(BOOK_ID, COPY_1))
            .expectException(ConflictException.class);
    }

    @Test
    void cannotRemoveOrDeleteWhileCopyInUse() {
        fixture.given(created(), copyAdded(COPY_1), reserved(COPY_1, "borrow-1"), borrowed(COPY_1, "borrow-1"))
            .when(new RemoveBookCopyCommand(BOOK_ID, COPY_1))
            .expectException(ConflictException.class);

        fixture.given(created(), copyAdded(COPY_1), reserved(COPY_1, "borrow-1"))
            .when(new DeleteBookCommand(BOOK_ID))
            .expectException(ConflictException.class);
    }

    @Test
    void barcodeMustBeUniqueWithinBook() {
        fixture.given(created(), new BookCopyAddedEvent(BOOK_ID, COPY_1, "BC001", null, null))
            .when(new AddBookCopyCommand(BOOK_ID, COPY_2, "BC001", null, null))
            .expectException(ConflictException.class);
    }
}
