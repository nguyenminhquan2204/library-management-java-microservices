package com.javamicroservices.paymentservice.command.data;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fine_payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinePayment {
    @Id 
    private String id;

    @Column(nullable = false)
    private String fineId;

    @Column(nullable = false)
    private String employeeId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    private String referenceCode;

    private String collectedBy;

    private String note;

    @Column(nullable = false)
    private Date paidAt;
}
