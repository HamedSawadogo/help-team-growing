package com.commerceops.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record OrderResponse(
        Long id,
        Long customerId,
        String customerName,
        String status,
        LocalDateTime orderDate,
        BigDecimal totalAmount,
        List<OrderItemResponse> items
) {
    public record OrderItemResponse(Long productId, String productName, int quantity, BigDecimal lineTotal) {
    }

    // Touches order.getItems() and, per item, item.getProduct().getName():
    // on a list endpoint this is what triggers the N+1 query pattern.
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getLineTotal()))
                .collect(Collectors.toList());
        return new OrderResponse(order.getId(), order.getCustomer().getId(), order.getCustomer().getName(),
                order.getStatus(), order.getOrderDate(), order.getTotalAmount(), items);
    }
}
