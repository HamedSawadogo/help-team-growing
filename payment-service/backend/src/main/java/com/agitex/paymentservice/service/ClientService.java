package com.agitex.paymentservice.service;

import com.agitex.paymentservice.model.Client;
import com.agitex.paymentservice.repository.ClientRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;


@Service
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    private final ApplicationEventPublisher applicationEventPublisher;



    public ClientService(ClientRepository clientRepository,
                         ApplicationEventPublisher applicationEventPublisher) {
        this.clientRepository = clientRepository;
        this.applicationEventPublisher = applicationEventPublisher;
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
        System.err.println(
                "[" + taskId + "] Transaction ending"
        );
    }
}