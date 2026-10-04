package com.agitex.paymentservice.domain.repositories;

import com.agitex.paymentservice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByClientIdOrderByCreatedAtDesc(Long clientId);

    boolean existsByIdempotencyKey(String key);

    Optional<Payment> findByIdempotencyKey(String key);
}
