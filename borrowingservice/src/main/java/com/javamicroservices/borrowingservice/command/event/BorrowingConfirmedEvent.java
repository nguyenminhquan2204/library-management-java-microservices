package com.javamicroservices.borrowingservice.command.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class BorrowingConfirmedEvent {
    private String id;

    private String bookId;

    private String bookCopyId;

    private String employeeId;

    private String reservationId;
}
