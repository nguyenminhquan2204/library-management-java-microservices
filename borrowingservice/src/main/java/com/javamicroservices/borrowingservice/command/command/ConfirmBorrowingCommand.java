package com.javamicroservices.borrowingservice.command.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class ConfirmBorrowingCommand {
    @TargetAggregateIdentifier 
    private String id;

    private String bookCopyId;

    private String reservationId;
}
