package com.javamicroservices.borrowingservice.command.data;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BorrowingRepository extends JpaRepository<Borrowing, String> {
    Optional<Borrowing> findByBookIdAndEmployeeIdAndReturnDateNull(String bookId, String employeeId);

    List<Borrowing> findByEmployeeId(String employeeId);

    /** Phiếu chưa trả, hạn trả nằm trong [from, to) và chưa được nhắc. */
    @Query("""
        SELECT b FROM Borrowing b
        WHERE b.returnDate IS NULL
          AND b.dueSoonNotified = false
          AND b.dueDate >= :from AND b.dueDate < :to
        """)
    List<Borrowing> findDueSoon(@Param("from") Date from, @Param("to") Date to);

    /** Phiếu chưa trả, đã quá hạn (hạn trả trước {@code startOfToday}) và hôm nay chưa được báo. */
    @Query("""
        SELECT b FROM Borrowing b
        WHERE b.returnDate IS NULL
          AND b.dueDate < :startOfToday
          AND (b.lastOverdueNotifiedAt IS NULL OR b.lastOverdueNotifiedAt < :startOfToday)
        """)
    List<Borrowing> findOverdueNotNotifiedToday(@Param("startOfToday") Date startOfToday);
}
