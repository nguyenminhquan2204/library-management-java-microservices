package com.javamicroservices.commonservice.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Saga yêu cầu BookAggregate giữ 1 bản sao AVAILABLE bất kỳ cho borrowing.
 * Route theo bookId -> mọi yêu cầu giữ cùng 1 đầu sách được BookAggregate xử lý tuần tự.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveBookCopyCommand {
    @TargetAggregateIdentifier
    private String bookId;

    private String reservationId;

    private String borrowingId;
}
