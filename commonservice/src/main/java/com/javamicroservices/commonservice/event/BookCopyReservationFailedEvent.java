package com.javamicroservices.commonservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookCopyReservationFailedEvent {
    private String bookId;

    private String reservationId;

    private String borrowingId;

    private String reason;
}
