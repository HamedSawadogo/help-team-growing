package com.agitex.paymentservice.controller;

import com.agitex.paymentservice.dto.ClientRequest;
import com.agitex.paymentservice.dto.ClientResponse;
import com.agitex.paymentservice.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
//
//    @PostMapping
//    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(request));
//    }
//
//    @GetMapping
//    public List<ClientResponse> getAllClients() {
//        return clientService.getAllClients();
//    }
//
//    @GetMapping("/{id}")
//    public ClientResponse getClientById(@PathVariable Long id) {
//        return clientService.getClientById(id);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
//        clientService.deleteClient(id);
//        return ResponseEntity.noContent().build();
//    }
}
