package com.agitex.paymentservice.application.payments;

import com.agitex.paymentservice.application.payments.dtos.PaymentRequest;
import com.agitex.paymentservice.domain.Payment;
import com.agitex.paymentservice.domain.PaymentMethod;
import com.agitex.paymentservice.domain.PaymentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("Default")
public class DefaultPaymentService implements PaymentGateway {

    @Override
    public Payment pay(PaymentRequest paymentRequest) {
        System.err.println("Default payment.....");
        return Payment.of(paymentRequest);
    }

    @Override
    public PaymentType provider() {
        return null;
    }

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return false;
    }
}
