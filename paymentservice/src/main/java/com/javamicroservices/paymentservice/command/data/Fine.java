package com.javamicroservices.paymentservice.command.data;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(
    name = "fines",
    uniqueConstraints = @UniqueConstraint(name = "uk_fine_borrowing_reason", columnNames = {"borrowing_id", "reason"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Fine {
    @Id 
    private String id;

    @Column(nullable = false)
    private String borrowingId;

    @Column(nullable = false)
    private String employeeId;

    @Column(nullable = false)
    private String bookId;

    @Column(nullable = false)
    private String bookCopyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FineReason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FineStatus status;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal waivedAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "VND";

    @Column(nullable = false)
    private Date assessedAt;

    private Date settledAt;

    private String waiveReason;

    private String waivedBy;

    @Transient 
    public BigDecimal getRemainingAmount() {
        return amount.subtract(paidAmount).subtract(waivedAmount);
    }
}
