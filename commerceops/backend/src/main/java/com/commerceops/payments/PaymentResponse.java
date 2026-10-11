package com.commerceops.payments;

import java.math.BigDecimal;

public record PaymentResponse(Long id, Long invoiceId, BigDecimal amount, String status, String providerReference) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getInvoiceId(), p.getAmount(), p.getStatus(), p.getProviderReference());
    }
}
