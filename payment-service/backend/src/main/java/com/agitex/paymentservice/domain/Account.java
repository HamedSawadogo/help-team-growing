package com.agitex.paymentservice.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String user;
    private int amount;
    private Instant createdAt;
    private String description;
    private String idempotencyKey;
    private String location;
    private boolean isActive;
}
