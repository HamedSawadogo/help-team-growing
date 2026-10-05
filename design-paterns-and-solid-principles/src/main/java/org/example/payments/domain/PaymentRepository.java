package org.example.payments.domain;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Optional<Payment> findByIdempotencyKey(UUID key);

    Payment save(Payment payment);
}
