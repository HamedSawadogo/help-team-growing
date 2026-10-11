package com.agitex.paymentservice.application.payments.dtos;

public record  WavePaymentDetails(String accountId, String accountKey) implements PaymentDetails {
 
}
