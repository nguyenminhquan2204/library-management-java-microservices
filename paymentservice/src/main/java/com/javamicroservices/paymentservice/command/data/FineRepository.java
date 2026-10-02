package com.javamicroservices.paymentservice.command.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FineRepository extends JpaRepository<Fine, String> {

    boolean existsByBorrowingIdAndReason(String borrowingId, FineReason reason);
}
