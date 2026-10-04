package com.agitex.paymentservice.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
