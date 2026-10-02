package com.javamicroservices.commonservice.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class BorrowingFineAssessedEvent {
    private String borrowingId;

    private String employeeId;

    private String bookId;

    private String bookCopyId;

    private String reason;

    private BigDecimal amount;
}
