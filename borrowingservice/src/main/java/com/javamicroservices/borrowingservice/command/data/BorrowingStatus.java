package com.javamicroservices.borrowingservice.command.data;

/**
 * PENDING -> CONFIRMED -> RETURNED
 * PENDING -> FAILED     (không giữ được bản sao / nhân viên bị khoá)
 * PENDING -> CANCELLED  (quá thời gian chờ saga)
 */
public enum BorrowingStatus {
    PENDING,
    CONFIRMED,
    RETURNED,
    FAILED,
    CANCELLED
}
