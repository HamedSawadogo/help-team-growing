package com.agitex.paymentservice.service;

import com.agitex.paymentservice.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionGatewayImpl implements TransactionGateway {

    private final AccountRepository accountRepository;

    public TransactionGatewayImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public int send(UUID from, UUID to, int amount) {
        return 0;
    }

    @Override
    @Transactional
    public int retrieve(int amount, UUID accountId) {
        return 0;
    }

    @Override
    @Transactional
    public int deposit(int amount, UUID accountId) {
        return 0;
    }
}
