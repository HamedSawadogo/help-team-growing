package com.agitex.paymentservice.application.payments;

import com.agitex.paymentservice.application.payments.dtos.PaymentRequest;
import com.agitex.paymentservice.domain.Payment;
import com.agitex.paymentservice.domain.PaymentMethod;
import com.agitex.paymentservice.domain.PaymentType;

public interface PaymentGateway {

    Payment pay(final PaymentRequest paymentRequest);

    PaymentType provider();

    boolean supports(PaymentMethod paymentMethod);
}
