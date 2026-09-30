package com.agitex.paymentservice.dto;

import com.agitex.paymentservice.model.Payment;
import com.agitex.paymentservice.model.PaymentMethod;
import com.agitex.paymentservice.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long clientId,
        String clientFullName,
        BigDecimal amount,
        String currency,
        PaymentMethod method,
        PaymentStatus status,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getClient().getId(),
                payment.getClient().getFirstName() + " " + payment.getClient().getLastName(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getDescription(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
