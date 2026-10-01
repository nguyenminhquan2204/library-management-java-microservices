package com.javamicroservices.commonservice.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class BookResponseCommonModel {
    private String id;

    private String name;

    private String author;

    private long totalCopies;

    private long availableCopies;

    private List<BookCopyResponseCommonModel> copies;
}
