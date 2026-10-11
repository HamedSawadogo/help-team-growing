package com.commerceops.orders;

import java.util.List;

public record CreateOrderRequest(Long customerId, List<OrderItemRequest> items) {
}
