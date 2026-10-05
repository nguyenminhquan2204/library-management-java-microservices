package com.javamicroservices.paymentservice.command.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FinePaymentRepository extends JpaRepository<FinePayment, String> {
}
