package org.example.infrastructure;

import org.example.domain.Client;
import org.example.domain.ClientQueryService;

import java.util.Optional;
import java.util.UUID;

public class DBClientQueryService implements ClientQueryService {
    private final ClientRepository clientRepository;

    @Override
    public Optional<Client> findById(UUID id) {
        return clientRepository.findById();
    }
}
