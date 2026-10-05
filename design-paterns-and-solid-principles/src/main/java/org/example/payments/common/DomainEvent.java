package org.example.payments.common;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID id();

    String eventType();

    Instant createdAt();
}
