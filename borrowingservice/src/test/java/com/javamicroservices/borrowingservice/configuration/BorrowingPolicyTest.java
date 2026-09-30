package com.javamicroservices.borrowingservice.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.javamicroservices.borrowingservice.configuration.BorrowingPolicyTestSupport.at;
import static com.javamicroservices.borrowingservice.configuration.BorrowingPolicyTestSupport.newPolicy;

class BorrowingPolicyTest {
    private BorrowingPolicy policy;

    @BeforeEach
    void setUp() {
        policy = newPolicy();
    }

    @Test
    void dueDateIsLoanDaysAfterBorrowing() {
        assertEquals(at(2026, 9, 15, 10), policy.dueDateFrom(at(2026, 9, 1, 10)));
    }

    @Test
    void overdueDaysCountCalendarDaysNotHours() {
        Date due = at(2026, 9, 15, 10);

        assertEquals(0, policy.overdueDays(due, at(2026, 9, 14, 23)));
        assertEquals(0, policy.overdueDays(due, at(2026, 9, 15, 23))); // trong ngày hạn trả vẫn chưa trễ
        assertEquals(1, policy.overdueDays(due, at(2026, 9, 16, 0)));
        assertEquals(3, policy.overdueDays(due, at(2026, 9, 18, 9)));
    }

    @Test
    void overdueDaysIsZeroWithoutDueDate() {
        assertEquals(0, policy.overdueDays(null, at(2026, 9, 18, 9)));
        assertEquals(BigDecimal.ZERO, policy.calculateFine(null, at(2026, 9, 18, 9)));
    }

    @Test
    void fineIsFinePerDayTimesOverdueDays() {
        Date due = at(2026, 9, 15, 10);

        assertEquals(BigDecimal.valueOf(15000), policy.calculateFine(due, at(2026, 9, 18, 9)));
        assertEquals(BigDecimal.ZERO, policy.calculateFine(due, at(2026, 9, 10, 9)));
    }

    @Test
    void sameDayAndStartOfDay() {
        assertTrue(policy.isSameDay(at(2026, 9, 15, 1), at(2026, 9, 15, 23)));
        assertFalse(policy.isSameDay(at(2026, 9, 15, 23), at(2026, 9, 16, 0)));
        assertEquals(at(2026, 9, 18, 0), policy.startOfDay(at(2026, 9, 15, 17), 3));
    }
}
