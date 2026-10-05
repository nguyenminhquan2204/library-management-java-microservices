package com.javamicroservices.paymentservice.command.event;

import java.math.BigDecimal;
import java.util.Date;

import com.javamicroservices.paymentservice.command.data.FineReason;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FineCreatedEvent {
    private String id;

    private String borrowingId;

    private String employeeId;

    private String bookId;

    private String bookCopyId;

    private FineReason reason;

    private BigDecimal amount;

    private Date assessedAt;
}
