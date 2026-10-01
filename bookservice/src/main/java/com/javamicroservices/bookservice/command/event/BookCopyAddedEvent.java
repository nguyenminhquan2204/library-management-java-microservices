package com.javamicroservices.bookservice.command.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class BookCopyAddedEvent {
    private String bookId;

    private String bookCopyId;

    private String barcode;

    private String location;

    private String condition;
}
