package com.javamicroservices.bookservice.command.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface BookCopyRepository extends JpaRepository<BookCopy, String> {
    List<BookCopy> findByBookIdOrderByCreatedAtAsc(String bookId);

    @Transactional
    void deleteByBookId(String bookId);
}
