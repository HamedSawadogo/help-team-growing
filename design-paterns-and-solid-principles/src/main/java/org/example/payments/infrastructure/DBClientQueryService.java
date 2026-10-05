package org.example.payments.infrastructure;

import org.example.clients.ClientRepository;
import org.example.payments.domain.Client;
import org.example.payments.domain.ClientQueryService;
import java.util.Optional;
import java.util.UUID;

public class DBClientQueryService implements ClientQueryService {
    private final ClientRepository clientRepository;

    public DBClientQueryService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Optional<Client> findById(UUID id) {
        return clientRepository.findById(id);
    }
}
