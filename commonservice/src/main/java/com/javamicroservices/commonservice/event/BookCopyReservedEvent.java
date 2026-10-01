package com.javamicroservices.commonservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookCopyReservedEvent {
    private String bookId;

    private String bookCopyId;

    private String reservationId;

    private String borrowingId;
}
