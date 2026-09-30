package com.javamicroservices.borrowingservice.query.projection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.javamicroservices.borrowingservice.command.data.Borrowing;
import com.javamicroservices.borrowingservice.command.data.BorrowingRepository;
import com.javamicroservices.borrowingservice.query.model.BorrowingResponseModel;
import com.javamicroservices.borrowingservice.query.queries.GetBorrowingWithEmployeeIdQuery;
import com.javamicroservices.commonservice.exception.NotFoundException;
import com.javamicroservices.commonservice.model.BookResponseCommonModel;
import com.javamicroservices.commonservice.model.EmployeeResponseCommonModel;
import com.javamicroservices.commonservice.queries.GetBookDetailQuery;
import com.javamicroservices.commonservice.queries.GetDetailEmployeeQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BorrowingProjection {

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private QueryGateway queryGateway;

    @QueryHandler
    public List<BorrowingResponseModel> handle(GetBorrowingWithEmployeeIdQuery query) {
        EmployeeResponseCommonModel employee = getEmployee(query.getEmployeeId());

        List<Borrowing> borrowings = borrowingRepository.findByEmployeeId(query.getEmployeeId());


        return borrowings.stream().map(borrowing -> {
            BookResponseCommonModel book = getBook(borrowing.getBookId());
            BorrowingResponseModel model = new BorrowingResponseModel();
            BeanUtils.copyProperties(borrowing, model);
            model.setEmployee(employee);
            model.setBook(book);
            return model;
        }).toList();
    }

    private EmployeeResponseCommonModel getEmployee(String employeeId) {
        try {
            GetDetailEmployeeQuery query = new GetDetailEmployeeQuery(employeeId);
            return queryGateway.query(query, ResponseTypes.instanceOf(EmployeeResponseCommonModel.class)).join();
        } catch (Exception e) {
            throw new NotFoundException("Employee not found with EmployeeId: " + employeeId);
        }
    }

    private BookResponseCommonModel getBook(String bookId) {
        try {
            GetBookDetailQuery query = new GetBookDetailQuery(bookId);
            return queryGateway.query(query, ResponseTypes.instanceOf(BookResponseCommonModel.class)).join();
        } catch (Exception e) {
            log.warn("Cannot get book detail for bookId {}: {}", bookId, e.getMessage());
            BookResponseCommonModel book = new BookResponseCommonModel();
            book.setId(bookId);
            return book;
        }
    }
}
