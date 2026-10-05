package com.agitex.paymentservice.application.orders;


import org.springframework.stereotype.Component;

@Component
public class CreateOderValidator {

    public void validate(CreateOrderCommand orderCommand) {

        if (orderCommand.items().isEmpty()) {
            throw new RuntimeException("");
        }

    }
}
