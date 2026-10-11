package com.commerceops.payments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stand-in for a real payment gateway. No real financial integration -
 * latency, jitter and failure rate are all configurable so incidents
 * involving a slow or flaky dependency can be reproduced on demand.
 */
@Component
public class PaymentProviderSimulator {

    private final long baseLatencyMs;
    private final long jitterMs;
    private final double failureRate;

    public PaymentProviderSimulator(
            @Value("${commerceops.payments.provider.base-latency-ms:150}") long baseLatencyMs,
            @Value("${commerceops.payments.provider.jitter-ms:100}") long jitterMs,
            @Value("${commerceops.payments.provider.failure-rate:0.03}") double failureRate) {
        this.baseLatencyMs = baseLatencyMs;
        this.jitterMs = jitterMs;
        this.failureRate = failureRate;
    }

    public record ProviderResult(boolean success, String reference) {
    }

    public ProviderResult charge(java.math.BigDecimal amount) {
        long latency = baseLatencyMs + (jitterMs > 0 ? ThreadLocalRandom.current().nextLong(jitterMs) : 0);
        try {
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean success = ThreadLocalRandom.current().nextDouble() >= failureRate;
        String reference = "PSP-" + UUID.randomUUID();
        return new ProviderResult(success, reference);
    }
}
