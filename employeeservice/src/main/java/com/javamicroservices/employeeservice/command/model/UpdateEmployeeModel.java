package com.javamicroservices.employeeservice.command.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class UpdateEmployeeModel {
    @NotBlank(message = "Firstname is mandatory")
    private String firstName;

    @NotBlank(message = "LastName is mandatory")
    private String lastName;

    @NotBlank(message = "Kin is mandatory")
    private String Kin;

    @NotNull(message = "IsDisciplined is mandatory")
    private Boolean isDisciplined;
}
