package com.commerceops.payments;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Page<Payment> findByInvoiceId(Long invoiceId, Pageable pageable);

    Page<Payment> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);
}
