package org.example.domain;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Optional<Payment> findByIdempotencyKey(UUID key);

    Payment create(Payment payment);
}
