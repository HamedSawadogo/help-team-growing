package com.agitex.paymentservice.service;

import com.agitex.paymentservice.dto.PaymentRequest;
import com.agitex.paymentservice.model.Payment;
import com.agitex.paymentservice.model.PaymentMethod;
import com.agitex.paymentservice.model.PaymentType;
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
