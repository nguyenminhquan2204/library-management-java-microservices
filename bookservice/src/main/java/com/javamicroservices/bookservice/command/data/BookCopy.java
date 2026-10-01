package com.javamicroservices.bookservice.command.data;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Read model của 1 bản sao vật lý. Trạng thái thật (để quyết định cho mượn) nằm trong BookAggregate,
 * bảng này chỉ được cập nhật từ event.
 */
@Entity
@Table(
    name = "book_copies",
    indexes = {
        @Index(name = "idx_book_copies_book_status", columnList = "book_id, status"),
        @Index(name = "idx_book_copies_barcode", columnList = "barcode")
    }
)
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class BookCopy {
    @Id 
    private String id;

    private String bookId;

    private String barcode;

    @Enumerated(EnumType.STRING)
    private BookCopyStatus status;

    // Borrowing đang giữ bản sao (RESERVED / BORROWED), null khi AVAILABLE
    private String borrowingId;

    private String location;

    // "condition" là từ khoá SQL
    @Column(name = "copy_condition")
    private String condition;

    private Date createdAt;

    private Date updatedAt;
}
