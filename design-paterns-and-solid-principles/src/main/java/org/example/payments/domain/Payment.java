package org.example.payments.domain;

import java.time.LocalDate;
import java.util.UUID;

public class Payment {
    private PaymentId id;
    private UUID idempotencyKey;
    private boolean isActive;
    private boolean isLocked;
    private LocalDate createdDate;
    private final int amount;
    private final Client client;

    private Payment(int amount, Client client) {
        this.amount = amount;
        this.client = client;
    }

    public static Payment of(int amount, Client client) {
        final Payment payment = new Payment(amount, client);
        payment.setCreatedDate(LocalDate.now());
        payment.setActive(true);
        payment.setLocked(false);
        return payment;
    }

    public PaymentId getId() {
        return id;
    }

    public void setId(PaymentId id) {
        this.id = id;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(UUID idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public int getAmount() {
        return amount;
    }
    public Client getClient() {
        return client;
    }
}
