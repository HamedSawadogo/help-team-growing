package com.agitex.paymentservice.service;

import com.agitex.paymentservice.dto.PaymentRequest;
import com.agitex.paymentservice.dto.PaymentResponse;
import com.agitex.paymentservice.model.Payment;
import com.agitex.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final List<PaymentGateway> paymentProviders;

    private final PaymentGatewayRegistry paymentGatewayRegistry;

    public PaymentService(PaymentRepository paymentRepository,
                          List<PaymentGateway> paymentProviders,
                          PaymentGatewayRegistry paymentGatewayRegistry) {
        this.paymentRepository = paymentRepository;
        this.paymentProviders = paymentProviders;
        this.paymentGatewayRegistry = paymentGatewayRegistry;
    }


    @Transactional
    public PaymentResponse makePayment(final PaymentRequest paymentRequest) {
        //TODO should be locked
        final Optional<Payment> paymentOptional = paymentRepository.findByIdempotencyKey(paymentRequest.idempotencyKey());
        if (paymentOptional.isPresent()) {
            return  PaymentResponse.from(paymentOptional.get());
        }
        validatePaymentRequest(paymentRequest);

        try {

            //final PaymentGateway paymentGateway = resolvePaymentGateway(paymentRequest);
            final Payment payment = paymentGatewayRegistry.get(paymentRequest.method()).pay(paymentRequest);

            paymentRepository.save(payment);
            return PaymentResponse.from(payment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

//    private PaymentGateway resolvePaymentGateway(PaymentRequest paymentRequest) {
//        return paymentProviders.stream()
//                 .filter(provider -> provider.supports(paymentRequest.method()))
//                 .findFirst().orElseThrow(
//                         () -> new RuntimeException("Payment Method not supported yey"));
//    }

    private void validatePaymentRequest(PaymentRequest paymentRequest) {
        if (!Objects.equals(paymentRequest.currency(), "XOF")) {
            throw new RuntimeException("Devise not supported");
        }
    }
}
