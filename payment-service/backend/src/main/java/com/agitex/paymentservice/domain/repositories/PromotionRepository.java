package com.agitex.paymentservice.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PromotionRepository extends JpaRepository<UUID, UUID> {
}
