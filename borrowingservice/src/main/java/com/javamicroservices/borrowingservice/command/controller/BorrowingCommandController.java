package com.javamicroservices.borrowingservice.command.controller;

import java.util.Date;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javamicroservices.borrowingservice.command.command.CreateBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.ReturnBorrowingCommand;
import com.javamicroservices.borrowingservice.command.command.UpdateBorrowingCommand;
import com.javamicroservices.borrowingservice.command.model.BorrowingCreateModel;
import com.javamicroservices.borrowingservice.command.model.BorrowingReturnModel;
import com.javamicroservices.borrowingservice.command.model.BorrowingUpdateModel;
import com.javamicroservices.borrowingservice.command.model.BorrowingUpdateResponse;
import com.javamicroservices.commonservice.model.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/borrowing")
public class BorrowingCommandController {

    @Autowired
    private CommandGateway commandGateway;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createBorrowing(@Valid @RequestBody BorrowingCreateModel model) {
        CreateBorrowingCommand command = new CreateBorrowingCommand(UUID.randomUUID().toString(), model.getBookId(), model.getEmployeeId(), new Date());
        String borrowingId = commandGateway.sendAndWait(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Borrowing created successfully", borrowingId));
    }

    @PatchMapping("/{borrowingId}")
    public ResponseEntity<ApiResponse<BorrowingUpdateResponse>> updateBorrowing(@PathVariable String borrowingId, @Valid @RequestBody BorrowingUpdateModel model) {
        UpdateBorrowingCommand command = new UpdateBorrowingCommand(borrowingId, model.getBookId(), model.getEmployeeId(), model.getBorrowingDate(), model.getReturnDate());
        BorrowingUpdateResponse result = commandGateway.sendAndWait(command);
        return ResponseEntity.ok(ApiResponse.success("Borrowing updated successfully", result));
    }

    @PatchMapping("/{borrowingId}/return")
    public ResponseEntity<ApiResponse<Void>> returnBorrowing(@PathVariable String borrowingId, @RequestBody BorrowingReturnModel  model) {
        ReturnBorrowingCommand command = new ReturnBorrowingCommand(borrowingId, model.getBookId(), model.getEmployeeId(), new Date());
        commandGateway.sendAndWait(command);
        return ResponseEntity.ok(ApiResponse.success("Return book successfully!", null));
    }
}
