package com.javamicroservices.borrowingservice.command.model;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class BorrowingUpdateModel {
    @NotBlank(message = "BookId is mandatory")
    private String bookId;

    @NotBlank(message = "EmployeeId is mandatory")
    private String employeeId;

    private Date borrowingDate;

    private Date returnDate;

    // Hạn trả mới (gia hạn). Bỏ trống thì giữ nguyên hạn cũ
    private Date dueDate;
} 
