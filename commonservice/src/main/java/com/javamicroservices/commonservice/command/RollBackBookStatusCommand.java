package com.javamicroservices.commonservice.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class RollBackBookStatusCommand {
    @TargetAggregateIdentifier 
    private String bookId;

    // Tên field phải trùng với BookRollBackStatusEvent vì BookAggregate copy bằng BeanUtils.copyProperties
    private Boolean isReady;

    private String employeeId;

    private String borrowingId;
}
