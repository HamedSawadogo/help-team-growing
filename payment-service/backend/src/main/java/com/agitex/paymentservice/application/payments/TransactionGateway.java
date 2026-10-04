package com.agitex.paymentservice.application.payments;


import java.util.UUID;

public interface TransactionGateway {

    int send(final UUID from, final UUID to, int amount);

    int retrieve(final int amount, final UUID accountId);

    int deposit(final int amount, final UUID accountId);
}
