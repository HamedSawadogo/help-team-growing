package com.agitex.paymentservice.domain;

import com.agitex.paymentservice.application.orders.CreateOrderItem;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Transient
    private List<OrderItem> items;

    public static Order create(List<CreateOrderItem> items) {
        Order order =  new Order();
        order.setItems(items.stream()
                .map(item -> new OrderItem(item.itemId(), item.price(), true))
                .collect(Collectors.toList()));
        return order;
    }
}
