package com.javamicroservices.commonservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class BookCopyResponseCommonModel {
    private String id;

    private String barcode;

    private String status;

    private String location;

    private String condition;
}
