package com.agitex.paymentservice.service;

import com.agitex.paymentservice.model.PaymentType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentGatewayRegistry {

    private final Map<PaymentType, PaymentGateway> paymentRegistry;

    public PaymentGatewayRegistry(List<PaymentGateway> paymentGateways) {
        this.paymentRegistry = paymentGateways.stream()
                .collect(Collectors.toMap(PaymentGateway::provider, Function.identity()));
    }

    public PaymentGateway get(PaymentType paymentType) {
        return Optional.ofNullable(paymentRegistry.get(paymentType))
                .orElseThrow(() -> new IllegalArgumentException("Payment Type not supported"));
    }
}
