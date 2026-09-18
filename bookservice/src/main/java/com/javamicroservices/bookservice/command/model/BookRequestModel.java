package com.javamicroservices.bookservice.command.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class BookRequestModel {
    private String id;

    @NotBlank(message = "Book name is mandatory")
    @Size (min = 2, max = 30, message = "Book name must be between 2 and 30 characters")
    private String name;

    @NotBlank(message = "Book author is mandatory")
    private String author;

    private Boolean isReady;
}
