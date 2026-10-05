package com.javamicroservices.paymentservice.command.event;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.commonservice.event.BorrowingFineAssessedEvent;
import com.javamicroservices.paymentservice.command.command.CreateFineCommand;
import com.javamicroservices.paymentservice.command.data.FineReason;

@Component
public class BorrowingFineAssessedHandler {

    @Autowired
    private CommandGateway commandGateway;

    @EventHandler
    public void on(BorrowingFineAssessedEvent event) {
        FineReason reason = FineReason.valueOf(event.getReason());
        CreateFineCommand command = new CreateFineCommand(
            fineIdOf(event.getBorrowingId(), reason),
            event.getBorrowingId(),
            event.getEmployeeId(),
            event.getBookId(),
            event.getBookCopyId(),
            reason,
            event.getAmount()
        );
        commandGateway.sendAndWait(command);
    }

    private String fineIdOf(String borrowingId, FineReason reason) {
        return UUID.nameUUIDFromBytes((borrowingId + ":" + reason).getBytes(StandardCharsets.UTF_8)).toString();
    }
}
