package com.agitex.paymentservice.domain.repositories;

import com.agitex.paymentservice.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findFirstByOrderByCreatedAtAsc();
}
