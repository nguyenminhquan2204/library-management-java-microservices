package com.javamicroservices.paymentservice.command.event;

import java.util.Date;
import java.util.UUID;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.commonservice.event.BorrowingFineAssessedEvent;
import com.javamicroservices.paymentservice.command.data.Fine;
import com.javamicroservices.paymentservice.command.data.FineReason;
import com.javamicroservices.paymentservice.command.data.FineRepository;
import com.javamicroservices.paymentservice.command.data.FineStatus;

@Component 
public class PaymentEventsHandler {

    @Autowired 
    private FineRepository fineRepository;

    @EventHandler 
    public void on(BorrowingFineAssessedEvent event) {
        FineReason reason = FineReason.valueOf(event.getReason());
        // Event có thể bị gửi lại / replay -> không tạo fine trùng cho cùng lượt mượn
        if (fineRepository.existsByBorrowingIdAndReason(event.getBorrowingId(), reason)) {
            return;
        }

        Fine fine = new Fine();
        fine.setId(UUID.randomUUID().toString());
        fine.setBorrowingId(event.getBorrowingId());
        fine.setEmployeeId(event.getEmployeeId());
        fine.setBookId(event.getBookId());
        fine.setBookCopyId(event.getBookCopyId());
        fine.setReason(reason);
        fine.setStatus(FineStatus.UNPAID);
        fine.setAmount(event.getAmount());
        fine.setAssessedAt(new Date());

        fineRepository.save(fine);
    }
}
