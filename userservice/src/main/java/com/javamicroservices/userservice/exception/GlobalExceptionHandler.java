package com.javamicroservices.userservice.exception;

import com.javamicroservices.userservice.dto.ApiResponse;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error instanceof FieldError fieldError
                        ? fieldError.getField() + ": " + error.getDefaultMessage()
                        : error.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, ApiResponse.badRequest("Validation failed", details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, ApiResponse.badRequest("Malformed request body"));
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<ApiResponse<Void>> handleBadParameter(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, ApiResponse.badRequest(ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ApiResponse.notFound(ex.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ofStatus(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage()));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        return build(ex.getStatus(), ofStatus(ex.getStatus(), ex.getMessage()));
    }

    /**
     * Lỗi từ @PreAuthorize -> 403 (tránh rơi vào handler Exception chung thành 500).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, ApiResponse.forbidden("You do not have permission to perform this action"));
    }

    /**
     * Lỗi 4xx từ Identity Provider (Keycloak) chưa được service xử lý riêng.
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeignException(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null || !status.is4xxClientError()) {
            return handleException(ex);
        }
        log.warn("Identity provider returned {}: {}", ex.status(), ex.contentUTF8());
        return build(status, ofStatus(status, "Identity provider error: " + status.getReasonPhrase()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ApiResponse.error(ex.getMessage()));
    }

    private ApiResponse<Void> ofStatus(HttpStatus status, String message) {
        return ApiResponse.ofError(status.value(), message, status.getReasonPhrase());
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, ApiResponse<Void> body) {
        return new ResponseEntity<>(body, status);
    }
}
