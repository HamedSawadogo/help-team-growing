package com.agitex.paymentservice.application.payments.dtos;

import java.math.BigDecimal;

public record AccountBalanceResponse(BigDecimal balance, String currency) {
}
