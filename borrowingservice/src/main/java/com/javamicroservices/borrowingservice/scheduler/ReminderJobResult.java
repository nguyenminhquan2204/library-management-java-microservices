package com.javamicroservices.borrowingservice.scheduler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReminderJobResult {
    private int dueSoonSent;

    private int overdueSent;

    private int skipped;
}
