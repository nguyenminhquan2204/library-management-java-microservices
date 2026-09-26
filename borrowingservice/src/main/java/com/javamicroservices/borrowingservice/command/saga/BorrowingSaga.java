package com.javamicroservices.borrowingservice.command.saga;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import com.javamicroservices.borrowingservice.command.command.DeleteBorrowingCommand;
import com.javamicroservices.borrowingservice.command.event.BorrowingCreatedEvent;
import com.javamicroservices.borrowingservice.command.event.BorrowingDeletedEvent;
import com.javamicroservices.commonservice.command.RollBackBookStatusCommand;
import com.javamicroservices.commonservice.command.UpdateStatusBookCommand;
import com.javamicroservices.commonservice.event.BookRollBackStatusEvent;
import com.javamicroservices.commonservice.event.BookUpdateStatusEvent;
import com.javamicroservices.commonservice.model.BookResponseCommonModel;
import com.javamicroservices.commonservice.model.EmployeeResponseCommonModel;
import com.javamicroservices.commonservice.queries.GetBookDetailQuery;
import com.javamicroservices.commonservice.queries.GetDetailEmployeeQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Saga 
public class BorrowingSaga {
    @Autowired 
    private transient CommandGateway commandGateway;

    @Autowired 
    private transient QueryGateway queryGateway;

    @StartSaga 
    @SagaEventHandler(associationProperty = "id")
    private void handle(BorrowingCreatedEvent event) {
        log.info("BorrowingCreatedEvent in saga for BookId: " + event.getBookId() + " : EmployeeId: " + event.getEmployeeId());

        try {
            GetBookDetailQuery getBookDetailQuery = new GetBookDetailQuery(event.getBookId());
            BookResponseCommonModel bookResponseCommonModel = queryGateway.query(getBookDetailQuery, ResponseTypes.instanceOf(BookResponseCommonModel.class)).join();
            if(!bookResponseCommonModel.getIsReady()) {
                throw new Exception("Book is unavailable");
            } else {
                SagaLifecycle.associateWith("bookId", event.getBookId());
                UpdateStatusBookCommand command = new UpdateStatusBookCommand(event.getBookId(), false, event.getEmployeeId(), event.getId());
                commandGateway.sendAndWait(command);
            }

        } catch (Exception e) {
            rollbackBorrowingRecord(event.getId());
            log.error(e.getMessage());
        }
    }

    @SagaEventHandler(associationProperty = "bookId")
    private void handle(BookUpdateStatusEvent event) {
        log.info("BookUpdateStatusEvent in Saga for bookId: " + event.getBookId());
        
        try {
            GetDetailEmployeeQuery query = new GetDetailEmployeeQuery(event.getEmployeeId());
            EmployeeResponseCommonModel employeeModel = queryGateway.query(query, ResponseTypes.instanceOf(EmployeeResponseCommonModel.class)).join();
            if(employeeModel.getIsDisciplined()) {
                throw new Exception("This employee is blocked");
            } else {
                log.info("Borrowing book is successfully!");
                SagaLifecycle.end();
            }
        } catch (Exception e) {
            rollbackBookStatus(event.getBookId(), event.getEmployeeId(), event.getBorrowingId());
            log.error(e.getMessage());
        }
    }

    private void rollbackBorrowingRecord(String id) {
        DeleteBorrowingCommand command = new DeleteBorrowingCommand(id);
        commandGateway.sendAndWait(command);
    }

    private void rollbackBookStatus(String bookId, String employeeId, String borrowingId) {
        SagaLifecycle.associateWith("bookId", bookId);
        RollBackBookStatusCommand command = new RollBackBookStatusCommand(bookId, true, employeeId, borrowingId);
        commandGateway.sendAndWait(command);
    }

    @SagaEventHandler(associationProperty = "bookId")
    private void handle(BookRollBackStatusEvent event) {
        log.info("BookRollBackStatusEvent in saga for bookId {}" + event.getBookId());
        rollbackBorrowingRecord(event.getBorrowingId());
    }

    @SagaEventHandler(associationProperty = "id")
    @EndSaga 
    private void handle(BorrowingDeletedEvent event) {
        log.info("BorrowingDeletedEvent in Saga for borrowingId {}" + event.getId());
    }
}
