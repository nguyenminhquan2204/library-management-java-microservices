package com.javamicroservices.borrowingservice.command.data;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(
    name = "borrowing",
    indexes = {
        @Index(name = "idx_borrowing_employee_book_return", columnList = "employee_id, book_id, return_date")
    }
)
@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class Borrowing {
    @Id 
    private String id;

    private String bookId;

    private String employeeId;

    private Date borrowingDate;

    private Date returnDate;
}
