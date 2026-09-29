package com.javamicroservices.userservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception cho các lỗi nghiệp vụ, mang theo HTTP status tương ứng.
 */
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
