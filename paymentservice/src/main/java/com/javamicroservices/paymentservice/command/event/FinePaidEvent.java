package com.javamicroservices.paymentservice.command.event;

import java.math.BigDecimal;
import java.util.Date;

import com.javamicroservices.paymentservice.command.data.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinePaidEvent {
    private String id;

    private String paymentId;

    private String employeeId;

    private BigDecimal amount;

    private PaymentMethod method;

    private String referenceCode;

    private String note;

    private String collectedBy;

    private Date paidAt;
}
