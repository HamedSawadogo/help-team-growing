package org.example.domain;

import java.util.Optional;
import java.util.UUID;

public interface ClientQueryService {
    Optional<Client> findById(UUID id);
}
