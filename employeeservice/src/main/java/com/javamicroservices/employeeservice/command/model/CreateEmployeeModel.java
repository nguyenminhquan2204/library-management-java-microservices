package com.javamicroservices.employeeservice.command.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEmployeeModel {
    @NotBlank(message = "Firstname is mandatory")
    private String firstName;

    @NotBlank(message = "LastName is mandatory")
    private String lastName;

    @NotBlank(message = "Kin is mandatory")
    private String Kin;

    // Dùng để gửi email nhắc hạn trả / thông báo tiền phạt
    @Email(message = "Email is invalid")
    private String email;
}
