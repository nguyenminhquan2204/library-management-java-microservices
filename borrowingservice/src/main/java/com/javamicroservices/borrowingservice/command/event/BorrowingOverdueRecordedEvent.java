package com.javamicroservices.borrowingservice.command.event;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class BorrowingOverdueRecordedEvent {
    private String id;

    private long overdueDays;

    private BigDecimal fineAmount;

    private Date recordedAt;
}
