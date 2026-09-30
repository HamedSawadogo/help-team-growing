package com.agitex.paymentservice.service;

import com.agitex.paymentservice.dto.PaymentRequest;
import com.agitex.paymentservice.dto.PaymentResponse;
import com.agitex.paymentservice.exception.InvalidPaymentStateException;
import com.agitex.paymentservice.exception.ResourceNotFoundException;
import com.agitex.paymentservice.model.Client;
import com.agitex.paymentservice.model.Payment;
import com.agitex.paymentservice.model.PaymentStatus;
import com.agitex.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ClientService clientService;

    public PaymentResponse createPayment(PaymentRequest request) {
       return null;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByClient(Long clientId) {
//        clientService.findClientOrThrow(clientId);
//        return paymentRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream().map(PaymentResponse::from)
//                .toList();
        return null;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        return PaymentResponse.from(findPaymentOrThrow(id));
    }

    public PaymentResponse processPayment(Long id) {
        Payment payment = findPaymentOrThrow(id);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Seul un paiement PENDING peut être traité (statut actuel : " + payment.getStatus() + ")");
        }
        boolean simulatedFailure = payment.getAmount().remainder(BigDecimal.valueOf(100))
                .compareTo(BigDecimal.ZERO) == 0;
        payment.setStatus(simulatedFailure ? PaymentStatus.FAILED : PaymentStatus.COMPLETED);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    public PaymentResponse refundPayment(Long id) {
        Payment payment = findPaymentOrThrow(id);
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStateException(
                    "Seul un paiement COMPLETED peut être remboursé (statut actuel : " + payment.getStatus() + ")");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public Payment findPaymentOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable avec l'id " + id));
    }
}
