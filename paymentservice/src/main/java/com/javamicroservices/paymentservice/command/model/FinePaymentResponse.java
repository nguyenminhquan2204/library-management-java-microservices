package com.javamicroservices.paymentservice.command.model;

import java.math.BigDecimal;

import com.javamicroservices.paymentservice.command.data.FineStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinePaymentResponse {
    private String fineId;

    private String paymentId;

    private BigDecimal amount;

    private BigDecimal paidAmount;

    private BigDecimal remainingAmount;

    private FineStatus status;
}
