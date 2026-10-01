package com.javamicroservices.bookservice.command.event;

import java.time.Instant;
import java.util.Date;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.bookservice.command.data.Book;
import com.javamicroservices.bookservice.command.data.BookCopy;
import com.javamicroservices.bookservice.command.data.BookCopyRepository;
import com.javamicroservices.bookservice.command.data.BookCopyStatus;
import com.javamicroservices.bookservice.command.data.BookRepository;
import com.javamicroservices.commonservice.event.BookCopyBorrowedEvent;
import com.javamicroservices.commonservice.event.BookCopyReleasedEvent;
import com.javamicroservices.commonservice.event.BookCopyReservedEvent;

@Component
public class BookEventsHandler {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @EventHandler
    public void on(BookCreatedEvent event, @Timestamp Instant timestamp) {
        Date at = Date.from(timestamp);
        bookRepository.save(new Book(event.getId(), event.getName(), event.getAuthor(), at, at));
    }

    @EventHandler
    public void on(BookUpdatedEvent event, @Timestamp Instant timestamp) {
        bookRepository.findById(event.getId()).ifPresent(book -> {
            book.setName(event.getName());
            book.setAuthor(event.getAuthor());
            book.setUpdatedAt(Date.from(timestamp));
            bookRepository.save(book);
        });
    }

    @EventHandler
    public void on(BookDeletedEvent event) {
        bookCopyRepository.deleteByBookId(event.getId());
        bookRepository.findById(event.getId()).ifPresent(book -> bookRepository.delete(book));
    }

    @EventHandler
    public void on(BookCopyAddedEvent event, @Timestamp Instant timestamp) {
        Date at = Date.from(timestamp);
        bookCopyRepository.save(new BookCopy(event.getBookCopyId(), event.getBookId(), event.getBarcode(), BookCopyStatus.AVAILABLE,
            null, event.getLocation(), event.getCondition(), at, at));
    }

    @EventHandler
    public void on(BookCopyRemovedEvent event) {
        bookCopyRepository.deleteById(event.getBookCopyId());
    }

    @EventHandler
    public void on(BookCopyMarkedLostEvent event, @Timestamp Instant timestamp) {
        updateCopy(event.getBookCopyId(), BookCopyStatus.LOST, null, timestamp);
    }

    @EventHandler
    public void on(BookCopyMarkedDamagedEvent event, @Timestamp Instant timestamp) {
        updateCopy(event.getBookCopyId(), BookCopyStatus.DAMAGED, null, timestamp);
    }

    @EventHandler
    public void on(BookCopyReservedEvent event, @Timestamp Instant timestamp) {
        updateCopy(event.getBookCopyId(), BookCopyStatus.RESERVED, event.getBorrowingId(), timestamp);
    }

    @EventHandler
    public void on(BookCopyBorrowedEvent event, @Timestamp Instant timestamp) {
        updateCopy(event.getBookCopyId(), BookCopyStatus.BORROWED, event.getBorrowingId(), timestamp);
    }

    @EventHandler
    public void on(BookCopyReleasedEvent event, @Timestamp Instant timestamp) {
        updateCopy(event.getBookCopyId(), BookCopyStatus.AVAILABLE, null, timestamp);
    }

    private void updateCopy(String bookCopyId, BookCopyStatus status, String borrowingId, Instant timestamp) {
        bookCopyRepository.findById(bookCopyId).ifPresent(copy -> {
            copy.setStatus(status);
            copy.setBorrowingId(borrowingId);
            copy.setUpdatedAt(Date.from(timestamp));
            bookCopyRepository.save(copy);
        });
    }
}
