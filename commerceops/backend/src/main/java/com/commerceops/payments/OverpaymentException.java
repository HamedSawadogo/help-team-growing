package com.commerceops.payments;

import java.math.BigDecimal;

public class OverpaymentException extends RuntimeException {

    public OverpaymentException(Long invoiceId, BigDecimal amount, BigDecimal remainingDue) {
        super("Payment of " + amount + " exceeds remaining due " + remainingDue + " for invoice " + invoiceId);
    }
}
