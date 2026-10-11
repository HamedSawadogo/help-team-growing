package com.agitex.paymentservice.application.payments;

import com.agitex.paymentservice.application.payments.dtos.PaymentDetails;
import com.agitex.paymentservice.application.payments.dtos.PaymentRequest;
import com.agitex.paymentservice.domain.Payment;
import com.agitex.paymentservice.domain.PaymentMethod;
import com.agitex.paymentservice.domain.PaymentType;

import java.util.Map;

public interface PaymentGateway<D extends  PaymentDetails> {

    Payment pay(final PaymentRequest paymentRequest);

    Payment processPayment(D paymentDetails, Double amount, String currency);

    PaymentType provider();

    boolean supports(PaymentMethod paymentMethod);
}