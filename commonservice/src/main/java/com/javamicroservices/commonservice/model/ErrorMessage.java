package com.javamicroservices.commonservice.model;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class ErrorMessage {
    private String code;

    private String message;

    private HttpStatus status;
}
