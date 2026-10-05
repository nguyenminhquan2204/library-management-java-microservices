package com.javamicroservices.paymentservice.command.controller;

import java.util.Date;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javamicroservices.commonservice.model.ApiResponse;
import com.javamicroservices.commonservice.security.SecurityUtils;
import com.javamicroservices.paymentservice.command.command.PayFineCommand;
import com.javamicroservices.paymentservice.command.model.FinePaymentResponse;
import com.javamicroservices.paymentservice.command.model.PaymentRequestModel;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payment")
@PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
public class PaymentCommandController {

    @Autowired
    private CommandGateway commandGateway;

    @PostMapping("/{fineId}/pay")
    public ResponseEntity<ApiResponse<FinePaymentResponse>> payFine(@PathVariable String fineId, @Valid @RequestBody PaymentRequestModel model) {
        PayFineCommand command = new PayFineCommand(
            fineId,
            UUID.randomUUID().toString(),
            model.getAmount(),
            model.getMethod(),
            model.getReferenceCode(),
            model.getNote(),
            SecurityUtils.currentUsername(),
            new Date()
        );
        FinePaymentResponse result = commandGateway.sendAndWait(command);
        return ResponseEntity.ok(ApiResponse.success("Fine paid successfully", result));
    }
}
