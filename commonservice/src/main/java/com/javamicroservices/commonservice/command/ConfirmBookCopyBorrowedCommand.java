package com.javamicroservices.commonservice.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmBookCopyBorrowedCommand {
    @TargetAggregateIdentifier
    private String bookId;

    private String bookCopyId;

    private String borrowingId;
}
