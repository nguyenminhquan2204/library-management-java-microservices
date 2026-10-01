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
import com.javamicroservices.borrowingservice.query.queries.GetBorrowingDetailQuery;
import com.javamicroservices.borrowingservice.query.queries.GetBorrowingWithEmployeeIdQuery;
import com.javamicroservices.commonservice.exception.ForbiddenException;
import com.javamicroservices.commonservice.model.ApiResponse;
import com.javamicroservices.commonservice.security.Roles;
import com.javamicroservices.commonservice.security.SecurityUtils;

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

    /**
     * Mượn sách chạy bất đồng bộ qua saga: client gọi API này để biết phiếu đã CONFIRMED hay FAILED / CANCELLED.
     */
    @GetMapping("/{borrowingId}")
    public ApiResponse<BorrowingResponseModel> getBorrowingDetail(@PathVariable String borrowingId) {
        GetBorrowingDetailQuery query = new GetBorrowingDetailQuery(borrowingId);
        BorrowingResponseModel result = queryGateway.query(query, ResponseTypes.instanceOf(BorrowingResponseModel.class)).join();
        if (!SecurityUtils.hasAnyRole(Roles.LIBRARIAN, Roles.ADMIN)
                && (result.getEmployee() == null || !result.getEmployee().getId().equals(SecurityUtils.currentEmployeeId()))) {
            throw new ForbiddenException("You can only view your own borrowings");
        }
        return ApiResponse.success("Get borrowing detail successfully", result);
    }
}
