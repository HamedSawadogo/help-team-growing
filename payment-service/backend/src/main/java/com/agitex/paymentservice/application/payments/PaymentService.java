package com.agitex.paymentservice.application.payments;

import com.agitex.paymentservice.application.payments.dtos.PaymentRequest;
import com.agitex.paymentservice.application.payments.dtos.PaymentResponse;
import com.agitex.paymentservice.domain.Account;
import com.agitex.paymentservice.domain.Payment;
import com.agitex.paymentservice.domain.exception.InvalidPaymentStateException;
import com.agitex.paymentservice.domain.repositories.AccountRepository;
import com.agitex.paymentservice.domain.repositories.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;



@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final AccountRepository accountRepository;

    private final List<PaymentGateway> paymentProviders;

    private final PaymentGatewayRegistry paymentGatewayRegistry;

    public PaymentService(PaymentRepository paymentRepository,
                          AccountRepository accountRepository,
                          List<PaymentGateway> paymentProviders,
                          PaymentGatewayRegistry paymentGatewayRegistry) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
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

        final Account account = accountRepository.findFirstByOrderByCreatedAtAsc()
                .orElseThrow(() -> new IllegalStateException("Aucun compte n'est configuré"));

        if (account.getAmount().compareTo(paymentRequest.amount()) < 0) {
            throw new InvalidPaymentStateException("Solde insuffisant pour effectuer ce paiement");
        }

        try {
            //final PaymentGateway paymentGateway = resolvePaymentGateway(paymentRequest);
            final Payment payment = paymentGatewayRegistry.get(paymentRequest.method()).pay(paymentRequest);

            account.setAmount(account.getAmount().subtract(paymentRequest.amount()));
            accountRepository.save(account);
            payment.setBalanceAfter(account.getAmount());

            paymentRepository.save(payment);
            return PaymentResponse.from(payment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> listPayments(int page, int size) {
        final Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return paymentRepository.findAll(pageable).map(PaymentResponse::from);
    }

    private void validatePaymentRequest(PaymentRequest paymentRequest) {
        if (!Objects.equals(paymentRequest.currency(), "XOF")) {
            throw new RuntimeException("Devise not supported");
        }
    }
}
