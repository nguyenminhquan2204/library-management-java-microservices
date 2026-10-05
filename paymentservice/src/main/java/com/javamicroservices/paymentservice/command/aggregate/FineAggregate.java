package com.javamicroservices.paymentservice.command.aggregate;

import java.math.BigDecimal;
import java.util.Date;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import com.javamicroservices.commonservice.exception.BadRequestException;
import com.javamicroservices.commonservice.exception.ConflictException;
import com.javamicroservices.paymentservice.command.command.CreateFineCommand;
import com.javamicroservices.paymentservice.command.command.PayFineCommand;
import com.javamicroservices.paymentservice.command.data.FineStatus;
import com.javamicroservices.paymentservice.command.event.FineCreatedEvent;
import com.javamicroservices.paymentservice.command.event.FinePaidEvent;
import com.javamicroservices.paymentservice.command.model.FinePaymentResponse;

@Aggregate
public class FineAggregate {
    @AggregateIdentifier
    private String id;

    private String employeeId;

    private FineStatus status;

    private BigDecimal amount;

    private BigDecimal paidAmount = BigDecimal.ZERO;

    private BigDecimal waivedAmount = BigDecimal.ZERO;

    public FineAggregate() {}

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    public void handle(CreateFineCommand command) {
        if (this.id != null) {
            return;
        }
        if (command.getAmount() == null || command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Fine amount must be greater than zero");
        }

        FineCreatedEvent event = new FineCreatedEvent();
        BeanUtils.copyProperties(command, event);
        event.setAssessedAt(new Date());
        AggregateLifecycle.apply(event);
    }

    @CommandHandler
    public FinePaymentResponse handle(PayFineCommand command) {
        if (this.status == FineStatus.PAID || this.status == FineStatus.WAIVED) {
            throw new ConflictException("Fine " + this.id + " is already " + this.status);
        }
        if (command.getAmount() == null || command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        BigDecimal remainingAmount = remainingAmount();
        if (command.getAmount().compareTo(remainingAmount) > 0) {
            throw new BadRequestException("Amount must be less than or equal to the remaining amount " + remainingAmount);
        }

        FinePaidEvent event = new FinePaidEvent();
        BeanUtils.copyProperties(command, event);
        event.setEmployeeId(this.employeeId);
        AggregateLifecycle.apply(event);
 
        // apply() chạy @EventSourcingHandler ngay -> state lúc này đã là state sau khi thanh toán
        return new FinePaymentResponse(this.id, command.getPaymentId(), command.getAmount(), this.paidAmount, remainingAmount(), this.status);
    }

    private BigDecimal remainingAmount() {
        return this.amount.subtract(this.paidAmount).subtract(this.waivedAmount);
    }

    @EventSourcingHandler
    public void on(FineCreatedEvent event) {
        this.id = event.getId();
        this.employeeId = event.getEmployeeId();
        this.amount = event.getAmount();
        this.status = FineStatus.UNPAID;
    }

    @EventSourcingHandler
    public void on(FinePaidEvent event) {
        this.paidAmount = this.paidAmount.add(event.getAmount());
        this.status = remainingAmount().compareTo(BigDecimal.ZERO) <= 0 ? FineStatus.PAID : FineStatus.PARTIALLY_PAID;
    }
}
