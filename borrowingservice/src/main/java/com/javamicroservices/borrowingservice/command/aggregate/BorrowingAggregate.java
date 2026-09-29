package com.javamicroservices.borrowingservice.command.aggregate;

import java.util.Date;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import com.javamicroservices.borrowingservice.command.command.CreateBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.DeleteBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.ReturnBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.UpdateBorrowingCommand;
import com.javamicroservices.borrowingservice.command.event.BorrowingCreatedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingDeletedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingReturedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingUpdatedEvent;
import com.javamicroservices.borrowingservice.command.model.BorrowingUpdateResponse;
import com.javamicroservices.commonservice.exception.BadRequestException;
import com.javamicroservices.commonservice.exception.ConflictException;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Aggregate 
public class BorrowingAggregate {
    @AggregateIdentifier 
    private String id;

    private String bookId;

    private String employeeId;

    private Date borrowingDate;

    private Date returnDate;

    public BorrowingAggregate() {}

    @CommandHandler 
    public BorrowingAggregate(CreateBorrowingCommand command) {
        BorrowingCreatedEvent event = new BorrowingCreatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @CommandHandler 
    public void handle(DeleteBorrowingCommand command) {
        BorrowingDeletedEvent event = new BorrowingDeletedEvent(command.getId());
        AggregateLifecycle.apply(event);
    }

    @CommandHandler 
    public void handle(ReturnBorrowingCommand command) {
        if (this.returnDate != null) {
            throw new ConflictException("Borrowing with bookId " + this.bookId + " already returned in " + this.returnDate);
        }
        if (!this.bookId.equals(command.getBookId()) || !this.employeeId.equals(command.getEmployeeId())) {
            throw new BadRequestException("BookId or employeeId does not match this borrowing");
        }

        BorrowingReturedEvent event = new BorrowingReturedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @CommandHandler
    public BorrowingUpdateResponse handle(UpdateBorrowingCommand command) {
        Date borrowingDate = command.getBorrowingDate() != null ? command.getBorrowingDate() : null;
        Date returnDate = command.getReturnDate() != null ? command.getReturnDate() : null;

        if (returnDate != null && returnDate.before(borrowingDate)) {
            throw new BadRequestException("Return date must be after borrowing date");
        }

        BorrowingUpdatedEvent event = new BorrowingUpdatedEvent(command.getId(), command.getBookId(), command.getEmployeeId(), borrowingDate, returnDate);
        AggregateLifecycle.apply(event);

        return new BorrowingUpdateResponse(this.id, this.bookId, this.employeeId, this.borrowingDate, this.returnDate);
    }

    @EventSourcingHandler
    public void on(BorrowingCreatedEvent event) {
        this.id = event.getId();
        this.bookId = event.getBookId();
        this.employeeId = event.getEmployeeId();
        this.borrowingDate = event.getBorrowingDate();
    }

    @EventSourcingHandler 
    public void on(BorrowingDeletedEvent event) {
        this.id = event.getId();
    }

    @EventSourcingHandler
    public void on(BorrowingUpdatedEvent event) {
        this.id = event.getId();
        this.bookId = event.getBookId();
        this.employeeId = event.getEmployeeId();
        this.borrowingDate = event.getBorrowingDate();
        this.returnDate = event.getReturnDate();
    }

    @EventSourcingHandler
    public void on(BorrowingReturedEvent event) {
        this.id = event.getId();
        this.bookId = event.getBookId();
        this.employeeId = event.getEmployeeId();
        this.returnDate = event.getReturnDate();
    }
}
