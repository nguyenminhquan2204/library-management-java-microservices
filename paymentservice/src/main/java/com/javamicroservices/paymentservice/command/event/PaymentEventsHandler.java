package com.javamicroservices.paymentservice.command.event;

import java.math.BigDecimal;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.commonservice.exception.NotFoundException;
import com.javamicroservices.paymentservice.command.data.Fine;
import com.javamicroservices.paymentservice.command.data.FinePayment;
import com.javamicroservices.paymentservice.command.data.FinePaymentRepository;
import com.javamicroservices.paymentservice.command.data.FineRepository;
import com.javamicroservices.paymentservice.command.data.FineStatus;

@Component
public class PaymentEventsHandler {

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private FinePaymentRepository finePaymentRepository;

    @EventHandler
    public void on(FineCreatedEvent event) {
        if (fineRepository.existsById(event.getId())) {
            return;
        }

        Fine fine = new Fine();
        fine.setId(event.getId());
        fine.setBorrowingId(event.getBorrowingId());
        fine.setEmployeeId(event.getEmployeeId());
        fine.setBookId(event.getBookId());
        fine.setBookCopyId(event.getBookCopyId());
        fine.setReason(event.getReason());
        fine.setStatus(FineStatus.UNPAID);
        fine.setAmount(event.getAmount());
        fine.setAssessedAt(event.getAssessedAt());

        fineRepository.save(fine);
    }

    @EventHandler
    public void on(FinePaidEvent event) {
        // Replay event -> lần thanh toán đã ghi rồi thì bỏ qua, tránh cộng tiền 2 lần
        if (finePaymentRepository.existsById(event.getPaymentId())) {
            return;
        }

        Fine fine = fineRepository.findById(event.getId()).orElseThrow(() -> new NotFoundException("Fine not found"));
        fine.setPaidAmount(fine.getPaidAmount().add(event.getAmount()));
        if (fine.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            fine.setStatus(FineStatus.PAID);
            fine.setSettledAt(event.getPaidAt());
        } else {
            fine.setStatus(FineStatus.PARTIALLY_PAID);
        }
        fineRepository.save(fine);

        FinePayment payment = new FinePayment();
        payment.setFineId(event.getId());
        payment.setId(event.getPaymentId());
        payment.setEmployeeId(event.getEmployeeId());
        payment.setAmount(event.getAmount());
        payment.setMethod(event.getMethod());
        payment.setReferenceCode(event.getReferenceCode());
        payment.setNote(event.getNote());
        payment.setCollectedBy(event.getCollectedBy());
        payment.setPaidAt(event.getPaidAt());
        finePaymentRepository.save(payment);
    }
}
