package com.agitex.paymentservice.service;

import com.agitex.paymentservice.dto.ClientRequest;
import com.agitex.paymentservice.dto.ClientResponse;
import com.agitex.paymentservice.exception.ResourceNotFoundException;
import com.agitex.paymentservice.model.Client;
import com.agitex.paymentservice.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public void createClientAndHoldConnection(int taskId) {

        Client client = new Client();
        client.setFirstName("Toe");
        client.setLastName("Jones");
        client.setEmail(UUID.randomUUID() + "@gmail.com");
        client.setCreatedAt(Instant.now());

        clientRepository.save(client);

        System.err.println(
                "[" + taskId + "] DB operation done - sleeping..."
        );

        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.err.println(
                "[" + taskId + "] Transaction ending"
        );
    }
}