package com.javamicroservices.bookservice.query.controller;

import java.util.List;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javamicroservices.bookservice.query.model.BookResponseModel;
import com.javamicroservices.bookservice.query.queries.GetAllBookQuery;
import com.javamicroservices.commonservice.model.BookResponseCommonModel;
import com.javamicroservices.commonservice.queries.GetBookDetailQuery;
import com.javamicroservices.commonservice.services.KafkaService;

@RestController 
@RequestMapping("/api/v1/books")
public class BookQueryController {
    @Autowired 
    private QueryGateway queryGateway;

    @Autowired 
    private KafkaService kafkaService;

    @GetMapping 
    public List<BookResponseModel> getAllBooks() {
        GetAllBookQuery query = new GetAllBookQuery();
        List<BookResponseModel> results = queryGateway.query(query, ResponseTypes.multipleInstancesOf(BookResponseModel.class)).join();
        return results;
    }

    @GetMapping("/{bookId}")
    public BookResponseCommonModel getBookDetail(@PathVariable String bookId) {
        GetBookDetailQuery query = new GetBookDetailQuery(bookId);
        return queryGateway.query(query, ResponseTypes.instanceOf(BookResponseCommonModel.class)).join();
    }

    @PostMapping("/sendMessage")
    public void sendMessage(@RequestBody String message) {
        kafkaService.sendMessage("test", message);
    }
}
