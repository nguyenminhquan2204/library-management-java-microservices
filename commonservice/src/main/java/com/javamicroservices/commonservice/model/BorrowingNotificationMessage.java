package com.javamicroservices.commonservice.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message Kafka (dạng JSON) borrowingservice gửi cho notificationservice để nhắc hạn trả / báo quá hạn.
 * Ngày được format sẵn (dd/MM/yyyy) để template email dùng trực tiếp.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BorrowingNotificationMessage {
    public static final String TOPIC = "borrowing-notification";

    public enum Type {
        DUE_SOON,
        OVERDUE
    }

    private Type type;

    private String borrowingId;

    private String recipientEmail;

    private String employeeName;

    private String bookId;

    private String bookName;

    private String borrowingDate;

    private String dueDate;

    private long overdueDays;

    private BigDecimal fineAmount;

    private BigDecimal finePerDay;

    private String currency;
}
