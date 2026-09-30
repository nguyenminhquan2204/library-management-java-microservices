package com.javamicroservices.borrowingservice.command.aggregate;

import java.math.BigDecimal;
import java.util.Date;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.javamicroservices.borrowingservice.command.command.CreateBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.NotifyBorrowingDueSoonCommand;
import com.javamicroservices.borrowingservice.command.command.RecordBorrowingOverdueCommand;
import com.javamicroservices.borrowingservice.command.command.ReturnBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.UpdateBorrowingCommand;
import com.javamicroservices.borrowingservice.command.event.BorrowingCreatedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingDueSoonNotifiedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingOverdueRecordedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingReturedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingUpdatedEvent;
import com.javamicroservices.borrowingservice.configuration.BorrowingPolicy;
import com.javamicroservices.commonservice.exception.BadRequestException;
import com.javamicroservices.commonservice.exception.ConflictException;

import static com.javamicroservices.borrowingservice.configuration.BorrowingPolicyTestSupport.at;
import static com.javamicroservices.borrowingservice.configuration.BorrowingPolicyTestSupport.newPolicy;

class BorrowingAggregateTest {
    private static final String ID = "borrowing-1";
    private static final String BOOK_ID = "book-1";
    private static final String EMPLOYEE_ID = "employee-1";

    private static final Date BORROWED_AT = at(2026, 9, 1, 10);
    private static final Date DUE_AT = at(2026, 9, 15, 10);

    private FixtureConfiguration<BorrowingAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(BorrowingAggregate.class);
        fixture.registerInjectableResource(newPolicy());
    }

    private BorrowingCreatedEvent created() {
        return new BorrowingCreatedEvent(ID, BOOK_ID, EMPLOYEE_ID, BORROWED_AT, DUE_AT);
    }

    @Test
    void createBorrowingStoresDueDate() {
        fixture.givenNoPriorActivity()
            .when(new CreateBorrowingCommand(ID, BOOK_ID, EMPLOYEE_ID, BORROWED_AT, DUE_AT))
            .expectSuccessfulHandlerExecution()
            .expectEvents(created());
    }

    @Test
    void returnLateLocksInFine() {
        Date returnedAt = at(2026, 9, 18, 9);
        fixture.given(created())
            .when(new ReturnBorrowingCommand(ID, BOOK_ID, EMPLOYEE_ID, returnedAt))
            .expectEvents(new BorrowingReturedEvent(ID, BOOK_ID, EMPLOYEE_ID, returnedAt, BigDecimal.valueOf(15000)));
    }

    @Test
    void returnOnTimeHasNoFine() {
        Date returnedAt = at(2026, 9, 15, 20);
        fixture.given(created())
            .when(new ReturnBorrowingCommand(ID, BOOK_ID, EMPLOYEE_ID, returnedAt))
            .expectEvents(new BorrowingReturedEvent(ID, BOOK_ID, EMPLOYEE_ID, returnedAt, BigDecimal.ZERO));
    }

    @Test
    void dueSoonReminderIsSentOnlyOnce() {
        Date now = at(2026, 9, 13, 8);
        fixture.given(created())
            .when(new NotifyBorrowingDueSoonCommand(ID, now))
            .expectEvents(new BorrowingDueSoonNotifiedEvent(ID, now));

        fixture.given(created(), new BorrowingDueSoonNotifiedEvent(ID, now))
            .when(new NotifyBorrowingDueSoonCommand(ID, at(2026, 9, 14, 8)))
            .expectException(ConflictException.class)
            .expectNoEvents();
    }

    @Test
    void noReminderAfterReturn() {
        fixture.given(created(), new BorrowingReturedEvent(ID, BOOK_ID, EMPLOYEE_ID, at(2026, 9, 10, 9), BigDecimal.ZERO))
            .when(new NotifyBorrowingDueSoonCommand(ID, at(2026, 9, 13, 8)))
            .expectException(ConflictException.class);

        fixture.given(created(), new BorrowingReturedEvent(ID, BOOK_ID, EMPLOYEE_ID, at(2026, 9, 10, 9), BigDecimal.ZERO))
            .when(new RecordBorrowingOverdueCommand(ID, at(2026, 9, 20, 8)))
            .expectException(ConflictException.class);
    }

    @Test
    void extendingDueDateAllowsNewReminder() {
        Date newDue = at(2026, 9, 22, 10);
        fixture.given(
                created(),
                new BorrowingDueSoonNotifiedEvent(ID, at(2026, 9, 13, 8)),
                new BorrowingUpdatedEvent(ID, BOOK_ID, EMPLOYEE_ID, BORROWED_AT, null, newDue))
            .when(new NotifyBorrowingDueSoonCommand(ID, at(2026, 9, 20, 8)))
            .expectEvents(new BorrowingDueSoonNotifiedEvent(ID, at(2026, 9, 20, 8)));
    }

    @Test
    void updateKeepsDueDateWhenNotProvided() {
        fixture.given(created())
            .when(new UpdateBorrowingCommand(ID, BOOK_ID, EMPLOYEE_ID, BORROWED_AT, null, null))
            .expectEvents(new BorrowingUpdatedEvent(ID, BOOK_ID, EMPLOYEE_ID, BORROWED_AT, null, DUE_AT));
    }

    @Test
    void updateRejectsDueDateBeforeBorrowingDate() {
        fixture.given(created())
            .when(new UpdateBorrowingCommand(ID, BOOK_ID, EMPLOYEE_ID, BORROWED_AT, null, at(2026, 8, 30, 10)))
            .expectException(BadRequestException.class);
    }

    @Test
    void recordOverdueCalculatesFine() {
        Date now = at(2026, 9, 18, 8);
        BorrowingOverdueRecordedEvent expected = new BorrowingOverdueRecordedEvent(ID, 3, BigDecimal.valueOf(15000), now);
        fixture.given(created())
            .when(new RecordBorrowingOverdueCommand(ID, now))
            .expectEvents(expected)
            .expectResultMessagePayload(expected);
    }

    @Test
    void recordOverdueRejectsNotYetOverdue() {
        fixture.given(created())
            .when(new RecordBorrowingOverdueCommand(ID, at(2026, 9, 15, 20)))
            .expectException(BadRequestException.class);
    }

    @Test
    void recordOverdueOncePerDay() {
        BorrowingOverdueRecordedEvent morning = new BorrowingOverdueRecordedEvent(ID, 3, BigDecimal.valueOf(15000), at(2026, 9, 18, 8));

        fixture.given(created(), morning)
            .when(new RecordBorrowingOverdueCommand(ID, at(2026, 9, 18, 17)))
            .expectException(ConflictException.class);

        Date nextDay = at(2026, 9, 19, 8);
        fixture.given(created(), morning)
            .when(new RecordBorrowingOverdueCommand(ID, nextDay))
            .expectEvents(new BorrowingOverdueRecordedEvent(ID, 4, BigDecimal.valueOf(20000), nextDay));
    }
}
