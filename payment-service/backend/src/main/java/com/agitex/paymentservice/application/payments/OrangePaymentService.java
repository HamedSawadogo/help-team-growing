package com.agitex.paymentservice.application.payments;

import com.agitex.paymentservice.application.payments.dtos.OrangeMoneyPaymentDetails;
import com.agitex.paymentservice.application.payments.dtos.PaymentDetails;
import com.agitex.paymentservice.application.payments.dtos.PaymentRequest;
import com.agitex.paymentservice.domain.Payment;
import com.agitex.paymentservice.domain.PaymentMethod;
import com.agitex.paymentservice.domain.PaymentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("Orange")
public class OrangePaymentService implements PaymentGateway<OrangeMoneyPaymentDetails> {

    @Override
    public Payment pay(PaymentRequest paymentRequest) {
        System.err.println("Payment with ORANGE MONEY");
        return Payment.of(paymentRequest);
    }

    @Override
    public Payment processPayment(OrangeMoneyPaymentDetails paymentDetails, Double amount, String currency) {
        return null;
    }


    @Override
    public PaymentType provider() {
        return PaymentType.ORANGE_MONEY;
    }

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return PaymentType.ORANGE_MONEY.name().equalsIgnoreCase(paymentMethod.name());
    }

}
