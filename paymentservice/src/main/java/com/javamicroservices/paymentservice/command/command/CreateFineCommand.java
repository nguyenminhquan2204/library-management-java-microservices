package com.javamicroservices.paymentservice.command.command;

import java.math.BigDecimal;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import com.javamicroservices.paymentservice.command.data.FineReason;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateFineCommand {
    @TargetAggregateIdentifier
    private String id;

    private String borrowingId;

    private String employeeId;

    private String bookId;

    private String bookCopyId;

    private FineReason reason;

    private BigDecimal amount;
}
