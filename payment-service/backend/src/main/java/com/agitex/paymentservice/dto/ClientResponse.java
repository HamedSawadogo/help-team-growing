package com.agitex.paymentservice.dto;

import com.agitex.paymentservice.model.Client;

import java.time.Instant;

public record ClientResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Instant createdAt
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone(),
                client.getCreatedAt()
        );
    }
}
