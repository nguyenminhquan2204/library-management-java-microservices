package com.javamicroservices.borrowingservice.command.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class DeleteBorrowingCommand {
    @TargetAggregateIdentifier 
    private String id;
}
