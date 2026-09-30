package com.javamicroservices.borrowingservice.command.event;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class BorrowingDueSoonNotifiedEvent {
    private String id;

    private Date notifiedAt;
}
