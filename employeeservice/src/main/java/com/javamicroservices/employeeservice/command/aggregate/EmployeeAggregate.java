package com.javamicroservices.employeeservice.command.aggregate;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import com.javamicroservices.employeeservice.command.command.CreateEmployeeCommand;
import com.javamicroservices.employeeservice.command.command.DeleteEmployeeCommand;
import com.javamicroservices.employeeservice.command.command.UpdateEmployeeCommand;
import com.javamicroservices.employeeservice.command.event.EmployeeCreatedEvent;
import com.javamicroservices.employeeservice.command.event.EmployeeDeletedEvent;
import com.javamicroservices.employeeservice.command.event.EmployeeUpdatedEvent;

import lombok.NoArgsConstructor;

@NoArgsConstructor 
@Aggregate 
public class EmployeeAggregate {
    @AggregateIdentifier 
    private String id;

    private String firstName;

    private String lastName;

    private String kin;

    private Boolean isDisciplined;

    @CommandHandler 
    public EmployeeAggregate(CreateEmployeeCommand command) {
        EmployeeCreatedEvent event = new EmployeeCreatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @CommandHandler 
    public void handle(UpdateEmployeeCommand command) {
        EmployeeUpdatedEvent event = new EmployeeUpdatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @CommandHandler 
    public void handle(DeleteEmployeeCommand command) {
        EmployeeDeletedEvent event = new EmployeeDeletedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler 
    public void on(EmployeeCreatedEvent event) {
        this.id = event.getId();
        this.firstName = event.getFirstName();
        this.lastName = event.getLastName();
        this.kin = event.getKin();
        this.isDisciplined = event.getIsDisciplined();
    }

    @EventSourcingHandler
    public void on(EmployeeUpdatedEvent event) {
        this.id = event.getId();
        this.firstName = event.getFirstName();
        this.lastName = event.getLastName();
        this.kin = event.getKin();
        this.isDisciplined = event.getIsDisciplined();
    }

    @EventSourcingHandler
    public void on(EmployeeDeletedEvent event) {
        this.id = event.getId();
    }
}
