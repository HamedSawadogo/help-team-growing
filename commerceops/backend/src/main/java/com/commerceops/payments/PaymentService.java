package com.commerceops.payments;

import com.commerceops.invoicing.Invoice;
import com.commerceops.invoicing.InvoiceRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentProviderSimulator providerSimulator;
    private final Counter paymentsSucceededCounter;
    private final Counter paymentsFailedCounter;

    public PaymentService(PaymentRepository paymentRepository,
                           InvoiceRepository invoiceRepository,
                           PaymentProviderSimulator providerSimulator,
                           MeterRegistry meterRegistry) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.providerSimulator = providerSimulator;
        this.paymentsSucceededCounter = Counter.builder("commerceops.payments.outcome")
                .tag("result", "succeeded")
                .description("Payments by outcome")
                .register(meterRegistry);
        this.paymentsFailedCounter = Counter.builder("commerceops.payments.outcome")
                .tag("result", "failed")
                .description("Payments by outcome")
                .register(meterRegistry);
        meterRegistry.gauge("commerceops.payments.pending", paymentRepository,
                repo -> repo.countByStatus("PENDING"));
    }

    /**
     * Idempotency is handled correctly: a replay with the same key short-circuits
     * before any charge is attempted.
     *
     * What is NOT handled correctly: applying the successful payment to the
     * invoice is a plain read-modify-write on invoice.paidAmount, with no
     * pessimistic lock and no optimistic retry. Two concurrent payments
     * against the same invoice can both read the same paidAmount and
     * overwrite each other's update - a lost update, not an idempotency bug.
     */
    @Transactional
    public Payment pay(PaymentRequest request) {
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return existing.get();
        }

        Invoice invoice = invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + request.invoiceId()));

        if (request.amount().compareTo(invoice.getRemainingDue()) > 0) {
            throw new OverpaymentException(invoice.getId(), request.amount(), invoice.getRemainingDue());
        }

        Payment payment = new Payment();
        payment.setInvoiceId(invoice.getId());
        payment.setIdempotencyKey(request.idempotencyKey());
        payment.setAmount(request.amount());
        payment.setStatus("PENDING");
        payment = paymentRepository.save(payment);

        PaymentProviderSimulator.ProviderResult result = providerSimulator.charge(request.amount());

        payment.setAttemptCount(payment.getAttemptCount() + 1);
        payment.setProviderReference(result.reference());
        payment.setUpdatedAt(LocalDateTime.now());

        if (result.success()) {
            payment.setStatus("SUCCEEDED");
            invoice.setPaidAmount(invoice.getPaidAmount().add(request.amount()));
            if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0) {
                invoice.setStatus("PAID");
            } else {
                invoice.setStatus("PARTIALLY_PAID");
            }
            invoiceRepository.save(invoice);
            paymentsSucceededCounter.increment();
        } else {
            payment.setStatus("FAILED");
            paymentsFailedCounter.increment();
        }

        return paymentRepository.save(payment);
    }

    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
    }
}
