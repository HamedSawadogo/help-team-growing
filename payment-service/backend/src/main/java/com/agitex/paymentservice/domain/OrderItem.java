package com.agitex.paymentservice.domain;

public class OrderItem {
    private String id;
    private int price;
    private boolean isActive;

    public OrderItem(String id, int price, boolean isActive) {
        this.id = id;
        this.price = price;
        this.isActive = isActive;
    }
}
