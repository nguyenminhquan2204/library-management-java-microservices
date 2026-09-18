package com.javamicroservices.bookservice.query.projection;

import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.BeanUtils;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.bookservice.command.data.BookRepository;
import com.javamicroservices.bookservice.query.model.BookResponseModel;
import com.javamicroservices.bookservice.query.queries.GetAllBookQuery;
import com.javamicroservices.bookservice.query.queries.GetBookDetailQuery;
import com.javamicroservices.bookservice.command.data.Book;

@Component 
public class BookProjection {
    @Autowired 
    private BookRepository bookRepository;

    @QueryHandler 
    public List<BookResponseModel> handle(GetAllBookQuery query) {
        List<Book> booksModel = bookRepository.findAll();
        List<BookResponseModel> booksResponseModel = new ArrayList<>();
        booksModel.forEach(book -> {
            BookResponseModel responseModel = new BookResponseModel();
            BeanUtils.copyProperties(book, responseModel);
            booksResponseModel.add(responseModel);
        });
        return booksResponseModel;
    }

    @QueryHandler 
    public BookResponseModel handle(GetBookDetailQuery query) {
        BookResponseModel responseModel = new BookResponseModel();
        bookRepository.findById(query.getId()).ifPresent(book -> {
            BeanUtils.copyProperties(book, responseModel);
        });
        return responseModel;
    }
}
