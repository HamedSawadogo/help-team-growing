package com.agitex.paymentservice.application.orders;


public class CreateOderValidator {

    private static void validate(CreateOrderCommand orderCommand) {

        if (orderCommand.items().isEmpty()) {
            throw new RuntimeException("");
        }

    }
}
