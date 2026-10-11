package com.commerceops.orders;

public record OrderItemRequest(Long productId, int quantity) {
}
