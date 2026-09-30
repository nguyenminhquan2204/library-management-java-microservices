package com.javamicroservices.borrowingservice.configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.test.util.ReflectionTestUtils;

/** Dựng BorrowingPolicy với cấu hình mặc định (14 ngày, nhắc trước 2 ngày, 5.000 VND/ngày) cho unit test. */
public final class BorrowingPolicyTestSupport {
    private BorrowingPolicyTestSupport() {
    }

    public static BorrowingPolicy newPolicy() {
        BorrowingPolicy policy = new BorrowingPolicy();
        ReflectionTestUtils.setField(policy, "loanDays", 14);
        ReflectionTestUtils.setField(policy, "reminderDaysBefore", 2);
        ReflectionTestUtils.setField(policy, "finePerDay", BigDecimal.valueOf(5000));
        ReflectionTestUtils.setField(policy, "currency", "VND");
        return policy;
    }

    public static Date at(int year, int month, int day, int hour) {
        return Date.from(LocalDateTime.of(year, month, day, hour, 0).atZone(ZoneId.systemDefault()).toInstant());
    }
}
