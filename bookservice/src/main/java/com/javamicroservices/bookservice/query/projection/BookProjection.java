package com.javamicroservices.bookservice.query.projection;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.bookservice.command.data.Book;
import com.javamicroservices.bookservice.command.data.BookCopy;
import com.javamicroservices.bookservice.command.data.BookCopyRepository;
import com.javamicroservices.bookservice.command.data.BookCopyStatus;
import com.javamicroservices.bookservice.command.data.BookRepository;
import com.javamicroservices.bookservice.query.model.BookResponseModel;
import com.javamicroservices.bookservice.query.queries.GetAllBookQuery;
import com.javamicroservices.commonservice.exception.NotFoundException;
import com.javamicroservices.commonservice.model.BookCopyResponseCommonModel;
import com.javamicroservices.commonservice.model.BookResponseCommonModel;
import com.javamicroservices.commonservice.queries.GetBookDetailQuery;

/**
 * totalCopies / availableCopies không lưu sẵn mà đếm từ book_copies, nên không thể lệch với trạng thái từng bản sao.
 */
@Component 
public class BookProjection {
    @Autowired 
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @QueryHandler 
    public List<BookResponseModel> handle(GetAllBookQuery query) {
        Map<String, List<BookCopy>> copiesByBook = bookCopyRepository.findAll().stream()
            .collect(Collectors.groupingBy(BookCopy::getBookId));

        return bookRepository.findAll().stream().map(book -> {
            List<BookCopy> copies = copiesByBook.getOrDefault(book.getId(), List.of());
            return new BookResponseModel(book.getId(), book.getName(), book.getAuthor(), copies.size(), countAvailable(copies));
        }).collect(Collectors.toList());
    }

    @QueryHandler 
    public BookResponseCommonModel handle(GetBookDetailQuery query) {
        Book book = bookRepository.findById(query.getId()).orElseThrow(() -> new NotFoundException("Book not found with BookId: " + query.getId()));
        List<BookCopy> copies = bookCopyRepository.findByBookIdOrderByCreatedAtAsc(book.getId());

        List<BookCopyResponseCommonModel> copyModels = copies.stream()
            .map(copy -> new BookCopyResponseCommonModel(copy.getId(), copy.getBarcode(), copy.getStatus().name(), copy.getLocation(), copy.getCondition()))
            .collect(Collectors.toList());
        return new BookResponseCommonModel(book.getId(), book.getName(), book.getAuthor(), copies.size(), countAvailable(copies), copyModels);
    }

    private long countAvailable(List<BookCopy> copies) {
        return copies.stream().filter(copy -> copy.getStatus() == BookCopyStatus.AVAILABLE).count();
    }
}
