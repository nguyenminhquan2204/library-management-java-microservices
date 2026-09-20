package com.javamicroservices.employeeservice.query.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class EmployeeResponseModel {
    private String id;

    private String firstName;

    private String lastName;

    private String kin;

    private Boolean isDisciplined;
}
