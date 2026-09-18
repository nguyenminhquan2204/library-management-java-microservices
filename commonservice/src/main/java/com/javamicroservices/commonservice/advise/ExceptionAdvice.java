package com.javamicroservices.commonservice.advise;

import com.javamicroservices.commonservice.model.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice 
public class ExceptionAdvice {
    
    @ExceptionHandler 
    public ResponseEntity<ErrorMessage> handleException(Exception ex) {
        return new ResponseEntity(new ErrorMessage("9999", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
