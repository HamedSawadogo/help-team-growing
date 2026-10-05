package org.example.payments.domain;

import org.example.payments.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record PaymentId(UUID id,
                        String eventType,
                        Instant createdAt) implements DomainEvent {

    public PaymentId {
       if (id == null) {
           throw new RuntimeException("Payment Id is Required");
       }
    }

    @Override
    public String eventType() {
        return "NOTHING";
    }
}
