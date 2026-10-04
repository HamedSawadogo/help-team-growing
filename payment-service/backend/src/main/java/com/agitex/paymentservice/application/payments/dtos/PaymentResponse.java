package com.agitex.paymentservice.application.payments.dtos;

import com.agitex.paymentservice.domain.Payment;
import com.agitex.paymentservice.domain.PaymentMethod;
import com.agitex.paymentservice.domain.PaymentStatus;

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
                payment.getClient() != null ? payment.getClient().getId() :  null,
                payment.getClient() != null ? payment.getClient().getFirstName() + " " + payment.getClient().getLastName() : "",
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
