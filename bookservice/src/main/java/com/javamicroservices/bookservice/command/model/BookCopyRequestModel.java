package com.javamicroservices.bookservice.command.model;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class BookCopyRequestModel {
    @Size(max = 50, message = "Barcode must be at most 50 characters")
    private String barcode;

    @Size(max = 100, message = "Location must be at most 100 characters")
    private String location;

    @Size(max = 100, message = "Condition must be at most 100 characters")
    private String condition;
}
