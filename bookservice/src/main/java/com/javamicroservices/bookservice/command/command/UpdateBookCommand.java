package com.javamicroservices.bookservice.command.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class UpdateBookCommand {
    @TargetAggregateIdentifier 
    private String id;

    private String name;

    private String author;

    private Boolean isReady;
}
