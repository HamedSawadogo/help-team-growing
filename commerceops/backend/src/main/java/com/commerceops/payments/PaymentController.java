package com.commerceops.payments;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentService paymentService, PaymentRepository paymentRepository) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public PaymentResponse pay(@RequestBody PaymentRequest request) {
        return PaymentResponse.from(paymentService.pay(request));
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable Long id) {
        return PaymentResponse.from(paymentService.getById(id));
    }

    @GetMapping
    public Page<PaymentResponse> search(
            @RequestParam(required = false) Long invoiceId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> payments;
        if (invoiceId != null) {
            payments = paymentRepository.findByInvoiceId(invoiceId, pageable);
        } else if (status != null) {
            payments = paymentRepository.findByStatus(status, pageable);
        } else {
            payments = paymentRepository.findAll(pageable);
        }
        return payments.map(PaymentResponse::from);
    }
}
