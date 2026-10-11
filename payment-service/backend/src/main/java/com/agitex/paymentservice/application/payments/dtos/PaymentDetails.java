package com.agitex.paymentservice.application.payments.dtos;


public sealed interface PaymentDetails permits OrangeMoneyPaymentDetails, WavePaymentDetails {

}


