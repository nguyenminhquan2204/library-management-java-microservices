package com.javamicroservices.borrowingservice.query.model;

import java.math.BigDecimal;
import java.util.Date;

import com.javamicroservices.borrowingservice.command.data.BorrowingStatus;
import com.javamicroservices.commonservice.model.BookResponseCommonModel;
import com.javamicroservices.commonservice.model.EmployeeResponseCommonModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowingResponseModel {
    private String id;

    private BorrowingStatus status;

    private String bookCopyId;

    // Lý do FAILED / CANCELLED
    private String failureReason;

    private Date borrowingDate;

    private Date returnDate;

    private Date dueDate;

    private BigDecimal fineAmount;

    private BookResponseCommonModel book;

    private EmployeeResponseCommonModel employee;
}
