package com.javamicroservices.paymentservice.command.command;

import java.math.BigDecimal;
import java.util.Date;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import com.javamicroservices.paymentservice.command.data.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayFineCommand {
    @TargetAggregateIdentifier
    private String id;

    // Id của lần thanh toán (bản ghi FinePayment), 1 fine có thể trả nhiều lần
    private String paymentId;

    private BigDecimal amount;

    private PaymentMethod method;

    private String referenceCode;

    private String note;

    // Username của thủ thư / admin thu tiền (lấy từ JWT)
    private String collectedBy;

    private Date paidAt;
}
