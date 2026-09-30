package com.agitex.paymentservice;

import com.agitex.paymentservice.model.Client;
import com.agitex.paymentservice.repository.ClientRepository;
import com.agitex.paymentservice.service.ClientService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.UUID;

@SpringBootApplication
public class PaymentServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner start(ClientService clientService) {
        return args -> {

            for (int i = 0; i < 100; i++) {

                int taskId = i;

                Thread.startVirtualThread(() -> {
                    System.err.println(
                            "[" + taskId + "] Virtual thread started"
                    );

                    clientService.createClientAndHoldConnection(taskId);
                });
            }

            System.err.println("*************** END ***********");
        };
    }

}
