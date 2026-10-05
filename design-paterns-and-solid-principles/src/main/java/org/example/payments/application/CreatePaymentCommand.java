package org.example.payments.application;

import java.util.UUID;

public record CreatePaymentCommand(int amount, UUID clientId,
                                   UUID idempotencyKey) {
}