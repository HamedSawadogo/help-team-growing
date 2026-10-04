package com.agitex.paymentservice.application.orders;

public class CreateOrderCommandHandler {

    private final CreateOderValidator createOderValidator;

    private final

    public CreateOrderCommandHandler(CreateOderValidator createOderValidator) {
        this.createOderValidator = createOderValidator;
    }

    public String create(CreateOrderCommand createOrderCommand) {

    }
}
