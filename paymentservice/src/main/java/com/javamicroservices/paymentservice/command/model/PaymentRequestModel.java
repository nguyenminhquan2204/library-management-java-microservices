package com.javamicroservices.paymentservice.command.model;

import java.math.BigDecimal;

import com.javamicroservices.paymentservice.command.data.PaymentMethod;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestModel {
    @NotNull(message = "Amount is mandatory")
    @Positive(message = "Amount must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Payment method is mandatory")
    private PaymentMethod method;

    private String referenceCode;

    private String note;
}
