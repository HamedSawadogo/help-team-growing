package org.example.application;

import java.util.UUID;

public record CreatePaymentCommand(int amount, UUID clientId,
                                   UUID idempotencyKey) {
}
