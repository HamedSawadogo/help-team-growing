package com.commerceops.payments;

import java.math.BigDecimal;

public record PaymentRequest(Long invoiceId, BigDecimal amount, String idempotencyKey) {
}
