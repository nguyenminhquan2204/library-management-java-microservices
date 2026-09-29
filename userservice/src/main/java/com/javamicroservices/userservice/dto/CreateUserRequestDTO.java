package com.javamicroservices.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateUserRequestDTO {
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email is invalid")
    private String email;

    @NotBlank(message = "Username is mandatory")
    private String username;

    private String firstName;

    private String lastName;

    private LocalDate dob;

    private String name;

    @NotBlank(message = "Password is mandatory")
    private String password;
}