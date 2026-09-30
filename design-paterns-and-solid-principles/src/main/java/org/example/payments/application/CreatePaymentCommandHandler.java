package org.example.application;

import org.example.common.CommandHandler;
import org.example.domain.ClientQueryService;
import org.example.domain.Payment;
import org.example.domain.PaymentId;
import org.example.domain.PaymentRepository;
import java.util.Optional;


public class CreatePaymentCommandHandler implements CommandHandler<CreatePaymentCommand, PaymentId> {

    private final PaymentRepository paymentRepository;
    private final ClientQueryService clientQueryService;

    public CreatePaymentCommandHandler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentId handle(CreatePaymentCommand command) {
        Optional<Payment> byIdempotencyKey =
                paymentRepository.findByIdempotencyKey(command.idempotencyKey());
        if (byIdempotencyKey.isPresent()) {
            return byIdempotencyKey.get().getId();
        }
        resolvePaymentClient(command);
        Payment newPayment = Payment.of(command.amount());
        Payment paymentCreated = paymentRepository.create(newPayment);
        return paymentCreated.getId();
    }
}
