package com.javamicroservices.borrowingservice.configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

/**
 * Luật mượn sách: hạn trả, thời điểm nhắc hạn, cách tính tiền phạt.
 * Mọi phép so sánh đều theo NGÀY (không theo giờ): trễ 1 ngày lịch = 1 ngày phạt.
 */
@Getter
@Component
public class BorrowingPolicy {
    @Value("${borrowing.policy.loan-days:14}")
    private int loanDays;

    @Value("${borrowing.policy.reminder-days-before:2}")
    private int reminderDaysBefore;

    @Value("${borrowing.policy.fine-per-day:5000}")
    private BigDecimal finePerDay;

    @Value("${borrowing.policy.currency:VND}")
    private String currency;

    private final ZoneId zone = ZoneId.systemDefault();

    public Date dueDateFrom(Date borrowingDate) {
        return Date.from(borrowingDate.toInstant().plus(loanDays, ChronoUnit.DAYS));
    }

    /** Số ngày trễ hạn tính tới thời điểm {@code at}; 0 nếu chưa trễ. */
    public long overdueDays(Date dueDate, Date at) {
        if (dueDate == null || at == null) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(toLocalDate(dueDate), toLocalDate(at)));
    }

    public BigDecimal calculateFine(Date dueDate, Date at) {
        return finePerDay.multiply(BigDecimal.valueOf(overdueDays(dueDate, at)));
    }

    public boolean isSameDay(Date first, Date second) {
        return toLocalDate(first).equals(toLocalDate(second));
    }

    /** 00:00 của ngày {@code at} cộng thêm {@code plusDays} ngày. */
    public Date startOfDay(Date at, int plusDays) {
        return Date.from(toLocalDate(at).plusDays(plusDays).atStartOfDay(zone).toInstant());
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(zone).toLocalDate();
    }
}
