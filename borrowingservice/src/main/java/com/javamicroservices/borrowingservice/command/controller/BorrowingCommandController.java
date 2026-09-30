package com.javamicroservices.borrowingservice.command.controller;

import java.util.Date;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.javamicroservices.commonservice.exception.ForbiddenException;
import com.javamicroservices.commonservice.model.ApiResponse;
import com.javamicroservices.commonservice.security.Roles;
import com.javamicroservices.commonservice.security.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/borrowing")
public class BorrowingCommandController {

    @Autowired
    private CommandGateway commandGateway;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createBorrowing(@Valid @RequestBody BorrowingCreateModel model) {
        checkEmployeeOwnership(model.getEmployeeId());
        CreateBorrowingCommand command = new CreateBorrowingCommand(UUID.randomUUID().toString(), model.getBookId(), model.getEmployeeId(), new Date());
        String borrowingId = commandGateway.sendAndWait(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Borrowing created successfully", borrowingId));
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @PatchMapping("/{borrowingId}")
    public ResponseEntity<ApiResponse<BorrowingUpdateResponse>> updateBorrowing(@PathVariable String borrowingId, @Valid @RequestBody BorrowingUpdateModel model) {
        UpdateBorrowingCommand command = new UpdateBorrowingCommand(borrowingId, model.getBookId(), model.getEmployeeId(), model.getBorrowingDate(), model.getReturnDate());
        BorrowingUpdateResponse result = commandGateway.sendAndWait(command);
        return ResponseEntity.ok(ApiResponse.success("Borrowing updated successfully", result));
    }

    @PatchMapping("/{borrowingId}/return")
    public ResponseEntity<ApiResponse<Void>> returnBorrowing(@PathVariable String borrowingId, @RequestBody BorrowingReturnModel  model) {
        // Aggregate đã kiểm tra employeeId khớp với phiếu mượn -> MEMBER chỉ trả được sách của chính mình
        checkEmployeeOwnership(model.getEmployeeId());
        ReturnBorrowingCommand command = new ReturnBorrowingCommand(borrowingId, model.getBookId(), model.getEmployeeId(), new Date());
        commandGateway.sendAndWait(command);
        return ResponseEntity.ok(ApiResponse.success("Return book successfully!", null));
    }

    /**
     * LIBRARIAN/ADMIN thao tác hộ bất kỳ nhân viên nào; các role khác chỉ được thao tác với employeeId của chính mình.
     */
    private void checkEmployeeOwnership(String employeeId) {
        if (SecurityUtils.hasAnyRole(Roles.LIBRARIAN, Roles.ADMIN)) {
            return;
        }
        String currentEmployeeId = SecurityUtils.currentEmployeeId();
        if (currentEmployeeId == null) {
            throw new ForbiddenException("Your account is not linked to any employee");
        }
        if (!currentEmployeeId.equals(employeeId)) {
            throw new ForbiddenException("You can only borrow or return books for yourself");
        }
    }
}
