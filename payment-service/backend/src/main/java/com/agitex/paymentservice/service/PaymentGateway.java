package com.agitex.paymentservice.service;

import com.agitex.paymentservice.dto.PaymentRequest;
import com.agitex.paymentservice.model.Payment;
import com.agitex.paymentservice.model.PaymentMethod;
import com.agitex.paymentservice.model.PaymentType;

public interface PaymentGateway {

    Payment pay(final PaymentRequest paymentRequest);

    PaymentType provider();

    boolean supports(PaymentMethod paymentMethod);
}
