package com.agitex.paymentservice.infrastructure.controller;

import com.agitex.paymentservice.application.payments.dtos.PaymentRequest;
import com.agitex.paymentservice.application.payments.dtos.PaymentResponse;
import com.agitex.paymentservice.application.payments.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.makePayment(request));
    }
}
