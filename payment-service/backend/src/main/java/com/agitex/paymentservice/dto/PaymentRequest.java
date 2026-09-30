package com.agitex.paymentservice.dto;

import com.agitex.paymentservice.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "Le client est obligatoire") Long clientId,
        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "0.01", message = "Le montant doit être positif") BigDecimal amount,
        String currency,
        @NotNull(message = "Le moyen de paiement est obligatoire") PaymentMethod method,
        String description
) {
}
