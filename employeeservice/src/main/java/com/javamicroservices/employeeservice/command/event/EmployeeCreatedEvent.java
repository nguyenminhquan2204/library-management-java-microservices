package com.javamicroservices.employeeservice.command.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class EmployeeCreatedEvent {
    private String id;

    private String firstName;

    private String lastName;

    private String kin;

    private Boolean isDisciplined;
}
