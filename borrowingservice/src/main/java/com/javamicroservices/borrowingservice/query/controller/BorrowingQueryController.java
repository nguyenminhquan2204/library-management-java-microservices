package com.javamicroservices.borrowingservice.query.controller;

import java.util.List;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javamicroservices.borrowingservice.query.model.BorrowingResponseModel;
import com.javamicroservices.borrowingservice.query.queries.GetBorrowingWithEmployeeIdQuery;
import com.javamicroservices.commonservice.model.ApiResponse;

@RestController 
@RequestMapping("/api/v1/borrowing")
public class BorrowingQueryController {

    @Autowired 
    private QueryGateway queryGateway;

    @GetMapping("/employeeId/{employeeId}")
    public ApiResponse<List<BorrowingResponseModel>> getBorrowingForMe(@PathVariable String employeeId) {
        GetBorrowingWithEmployeeIdQuery query = new GetBorrowingWithEmployeeIdQuery(employeeId);
        List<BorrowingResponseModel> results = queryGateway.query(query, ResponseTypes.multipleInstancesOf(BorrowingResponseModel.class)).join();
        return ApiResponse.success("Get Borrowing with employeeId successfully", results);
    }
}
