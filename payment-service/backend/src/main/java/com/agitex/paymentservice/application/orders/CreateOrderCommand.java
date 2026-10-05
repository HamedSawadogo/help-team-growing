package com.agitex.paymentservice.application.orders;

import java.util.List;

public record CreateOrderCommand(
        List<CreateOrderItem> items,
        String promotionCode, String description) {
}

