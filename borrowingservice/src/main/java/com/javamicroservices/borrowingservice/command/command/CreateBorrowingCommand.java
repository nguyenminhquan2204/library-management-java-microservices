package com.javamicroservices.borrowingservice.command.command;

import java.util.Date;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class CreateBorrowingCommand {
    @TargetAggregateIdentifier 
    private String id;

    private String bookId;

    private String employeeId;

    private Date borrowingDate;
}
