package com.agitex.paymentservice.repository;

import com.agitex.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByClientIdOrderByCreatedAtDesc(Long clientId);
}
