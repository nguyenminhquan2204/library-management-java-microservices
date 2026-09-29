package com.javamicroservices.borrowingservice.command.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowingRepository extends JpaRepository<Borrowing, String> {
    Optional<Borrowing> findByBookIdAndEmployeeIdAndReturnDateNull(String bookId, String employeeId);
}
