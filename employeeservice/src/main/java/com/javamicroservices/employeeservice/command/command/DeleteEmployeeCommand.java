package com.javamicroservices.employeeservice.command.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class DeleteEmployeeCommand {
    @TargetAggregateIdentifier 
    private String id;
}
