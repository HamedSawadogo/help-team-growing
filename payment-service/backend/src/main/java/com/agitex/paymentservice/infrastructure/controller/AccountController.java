package com.agitex.paymentservice.infrastructure.controller;

import com.agitex.paymentservice.application.payments.dtos.AccountBalanceResponse;
import com.agitex.paymentservice.domain.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;

    @GetMapping("/balance")
    public AccountBalanceResponse getBalance() {
        return accountRepository.findFirstByOrderByCreatedAtAsc()
                .map(account -> new AccountBalanceResponse(account.getAmount(), "XOF"))
                .orElseGet(() -> new AccountBalanceResponse(java.math.BigDecimal.ZERO, "XOF"));
    }
}
