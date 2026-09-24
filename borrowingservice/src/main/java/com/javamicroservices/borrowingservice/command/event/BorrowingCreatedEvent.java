package com.javamicroservices.borrowingservice.command.event;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class BorrowingCreatedEvent {
    private String id;

    private String bookId;

    private String employeeId;

    private Date borrowingDate;
}
