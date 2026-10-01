package com.javamicroservices.borrowingservice.command.event;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class BorrowingReturnedEvent {
    private String id;
    
    private String bookId;

    // Bản sao đang được mượn, saga release đúng bản này
    private String bookCopyId;

    private String employeeId;

    private Date returnDate;

    private BigDecimal fineAmount;
}
