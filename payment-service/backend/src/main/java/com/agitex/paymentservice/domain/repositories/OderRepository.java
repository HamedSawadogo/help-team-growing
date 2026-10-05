package com.agitex.paymentservice.domain.repositories;

import com.agitex.paymentservice.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OderRepository extends JpaRepository<Order, UUID> {
}
