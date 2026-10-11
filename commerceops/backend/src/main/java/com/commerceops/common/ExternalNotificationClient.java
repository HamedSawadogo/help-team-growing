package com.commerceops.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Stand-in for an outbound call to a notification/email provider.
 * Simulates network latency with a blocking sleep - this is called
 * synchronously from inside order creation, holding the DB transaction
 * open for the duration.
 */
@Component
public class ExternalNotificationClient {

    private final long latencyMs;

    public ExternalNotificationClient(@Value("${commerceops.notifications.latency-ms:200}") long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public void sendOrderConfirmation(Long customerId, Long orderId) {
        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
