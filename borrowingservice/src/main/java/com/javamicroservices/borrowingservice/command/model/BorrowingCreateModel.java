package com.javamicroservices.borrowingservice.command.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowingCreateModel {
    @NotBlank(message = "BookId is mandatory")
    private String bookId;

    @NotBlank(message = "EmployeeId is mandatory")
    private String employeeId;
}
