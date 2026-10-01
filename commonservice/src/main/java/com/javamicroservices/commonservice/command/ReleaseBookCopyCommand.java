package com.javamicroservices.commonservice.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Trả bản sao về AVAILABLE: dùng khi trả sách (BORROWED) hoặc compensation (RESERVED).
 * Chỉ release được bản sao đang thuộc đúng borrowingId.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReleaseBookCopyCommand {
    @TargetAggregateIdentifier
    private String bookId;

    private String bookCopyId;

    private String borrowingId;
}
