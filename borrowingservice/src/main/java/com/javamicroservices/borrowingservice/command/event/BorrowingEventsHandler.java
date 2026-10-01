package com.javamicroservices.borrowingservice.command.event;

import java.util.Objects;
import java.util.Optional;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.borrowingservice.command.data.Borrowing;
import com.javamicroservices.borrowingservice.command.data.BorrowingRepository;
import com.javamicroservices.borrowingservice.command.data.BorrowingStatus;

@Component 
public class BorrowingEventsHandler {
    @Autowired 
    private BorrowingRepository borrowingRepository;

    @EventHandler 
    public void on(BorrowingCreatedEvent event) {
        Borrowing model = new Borrowing();
        BeanUtils.copyProperties(event, model);
        model.setStatus(BorrowingStatus.PENDING);
        borrowingRepository.save(model);
    }

    @EventHandler
    public void on(BorrowingConfirmedEvent event) {
        borrowingRepository.findById(event.getId()).ifPresent(borrowing -> {
            borrowing.setStatus(BorrowingStatus.CONFIRMED);
            borrowing.setBookCopyId(event.getBookCopyId());
            borrowing.setReservationId(event.getReservationId());
            borrowingRepository.save(borrowing);
        });
    }

    @EventHandler
    public void on(BorrowingFailedEvent event) {
        borrowingRepository.findById(event.getId()).ifPresent(borrowing -> {
            borrowing.setStatus(BorrowingStatus.FAILED);
            borrowing.setFailureReason(event.getReason());
            borrowingRepository.save(borrowing);
        });
    }

    @EventHandler
    public void on(BorrowingCancelledEvent event) {
        borrowingRepository.findById(event.getId()).ifPresent(borrowing -> {
            borrowing.setStatus(BorrowingStatus.CANCELLED);
            borrowing.setFailureReason(event.getReason());
            borrowingRepository.save(borrowing);
        });
    }

    @EventHandler
    public void on(BorrowingUpdatedEvent event) {
        Optional<Borrowing> oldEntity = borrowingRepository.findById(event.getId());
        oldEntity.ifPresent(borrowing -> {
            if (!Objects.equals(event.getDueDate(), borrowing.getDueDate())) {
                // Gia hạn -> sẽ được nhắc lại theo hạn mới
                borrowing.setDueSoonNotified(false);
            }
            BeanUtils.copyProperties(event, borrowing);
            borrowingRepository.save(borrowing);
        });
    }

    @EventHandler
    public void on(BorrowingReturnedEvent event) {
        Optional<Borrowing> oldEntity = borrowingRepository.findById(event.getId());
        oldEntity.ifPresent(borrowing -> {
            borrowing.setStatus(BorrowingStatus.RETURNED);
            borrowing.setReturnDate(event.getReturnDate());
            if (event.getFineAmount() != null) {
                borrowing.setFineAmount(event.getFineAmount());
            }
            borrowingRepository.save(borrowing);
        });
    }

    @EventHandler
    public void on(BorrowingDueSoonNotifiedEvent event) {
        borrowingRepository.findById(event.getId()).ifPresent(borrowing -> {
            borrowing.setDueSoonNotified(true);
            borrowingRepository.save(borrowing);
        });
    }

    @EventHandler
    public void on(BorrowingOverdueRecordedEvent event) {
        borrowingRepository.findById(event.getId()).ifPresent(borrowing -> {
            borrowing.setFineAmount(event.getFineAmount());
            borrowing.setLastOverdueNotifiedAt(event.getRecordedAt());
            borrowingRepository.save(borrowing);
        });
    }
}
