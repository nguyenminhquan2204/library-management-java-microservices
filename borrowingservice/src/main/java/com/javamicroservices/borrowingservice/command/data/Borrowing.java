package com.javamicroservices.borrowingservice.command.data;

import java.math.BigDecimal;
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
        @Index(name = "idx_borrowing_employee_book_return", columnList = "employee_id, book_id, return_date"),
        @Index(name = "idx_borrowing_return_due", columnList = "return_date, due_date")
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

    private Date dueDate;

    private Date returnDate;

    // Tiền phạt quá hạn: cập nhật mỗi lần cronjob ghi nhận quá hạn, chốt lại khi trả sách
    private BigDecimal fineAmount = BigDecimal.ZERO;

    // Đã gửi email nhắc sắp đến hạn chưa (reset khi hạn trả bị đổi)
    private Boolean dueSoonNotified = false;

    // Lần gần nhất gửi email quá hạn, để mỗi ngày chỉ gửi 1 lần
    private Date lastOverdueNotifiedAt;
}
