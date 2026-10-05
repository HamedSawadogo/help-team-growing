package org.example.payments.application;

import org.example.payments.common.CommandHandler;
import org.example.payments.domain.*;

import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import java.util.UUID;


public class CreatePaymentCommandHandler implements CommandHandler<CreatePaymentCommand, PaymentId> {

    private final PaymentRepository paymentRepository;
    private final ClientQueryService clientQueryService;

    public CreatePaymentCommandHandler(PaymentRepository paymentRepository,
                                       ClientQueryService clientQueryService) {
        this.paymentRepository = paymentRepository;
        this.clientQueryService = clientQueryService;
    }

    @Override
    public PaymentId handle(CreatePaymentCommand command) {
        Optional<Payment> byIdempotencyKey =
                paymentRepository.findByIdempotencyKey(command.idempotencyKey());
        if (byIdempotencyKey.isPresent()) {
            return byIdempotencyKey.get().getId();
        }

        final Client client = resolveClient(command.clientId());
        Payment newPayment = Payment.of(command.amount(), client);
        Payment paymentCreated = paymentRepository.save(newPayment);
        return paymentCreated.getId();
    }

    private Client resolveClient(UUID clientId) {
        return clientQueryService.findById(clientId).orElseThrow(
                () -> new RuntimeException("Client with id" + clientId + " not found")
        );
    }
}
