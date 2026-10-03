package com.agitex.paymentservice.repository;

import com.agitex.paymentservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
