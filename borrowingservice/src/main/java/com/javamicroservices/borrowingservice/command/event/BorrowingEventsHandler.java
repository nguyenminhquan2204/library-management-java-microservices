package com.javamicroservices.borrowingservice.command.event;

import java.util.Optional;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.borrowingservice.command.data.Borrowing;
import com.javamicroservices.borrowingservice.command.data.BorrowingRepository;

@Component 
public class BorrowingEventsHandler {
    @Autowired 
    private BorrowingRepository borrowingRepository;

    @EventHandler 
    public void on(BorrowingCreatedEvent event) {
        Borrowing model = new Borrowing();
        BeanUtils.copyProperties(event, model);
        borrowingRepository.save(model);
    }

    @EventHandler 
    public void on(BorrowingDeletedEvent event) {
        Optional<Borrowing> oldEntity = borrowingRepository.findById(event.getId());
        oldEntity.ifPresent(borrowing -> borrowingRepository.delete(borrowing));
    }
}
