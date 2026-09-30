package com.javamicroservices.commonservice.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.javamicroservices.commonservice.model.ApiResponse;

/**
 * Lỗi từ @PreAuthorize ném ra trong controller -> 403.
 * Đặt riêng (và ưu tiên cao hơn ExceptionAdvice) để không bị handler Exception chung biến thành 500,
 * đồng thời không làm hỏng service không dùng Spring Security.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(name = "org.springframework.security.access.AccessDeniedException")
public class SecurityExceptionAdvice {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>(ApiResponse.forbidden("You do not have permission to perform this action"), HttpStatus.FORBIDDEN);
    }
}
