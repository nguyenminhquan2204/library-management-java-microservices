package com.javamicroservices.bookservice.command.event;

import java.util.Optional;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.bookservice.command.data.Book;
import com.javamicroservices.bookservice.command.data.BookRepository;

@Component 
public class BookEventsHandler {

    @Autowired 
    private BookRepository bookRepository;

    @EventHandler 
    public void on(BookCreatedEvent event) {
        Book book = new Book();
        BeanUtils.copyProperties(event, book);
        bookRepository.save(book);
    }

    @EventHandler
    public void on(BookUpdatedEvent event) {
        Optional<Book> oldBook = bookRepository.findById(event.getId());
        if (oldBook.isPresent()) {
            Book book = oldBook.get();
            BeanUtils.copyProperties(event, book);
            bookRepository.save(book);
        }
    }

    @EventHandler 
    public void on(BookDeletedEvent event) {
        Optional<Book> oldBook = bookRepository.findById(event.getId());
        if (oldBook.isPresent()) {
            Book book = oldBook.get();
            bookRepository.delete(book);
        }
    }
}
