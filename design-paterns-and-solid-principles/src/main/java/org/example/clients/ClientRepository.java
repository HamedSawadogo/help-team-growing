package org.example.clients;

import org.example.payments.domain.Client;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {
    Optional<Client> findById(UUID id);
}
