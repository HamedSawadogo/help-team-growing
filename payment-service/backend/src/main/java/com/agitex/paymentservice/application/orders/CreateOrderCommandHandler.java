package com.agitex.paymentservice.application.orders;

import com.agitex.paymentservice.domain.Order;
import com.agitex.paymentservice.domain.repositories.OderRepository;
import com.agitex.paymentservice.domain.repositories.PromotionRepository;

public class CreateOrderCommandHandler {

    private final CreateOderValidator createOderValidator;

    private final OderRepository oderRepository;

    private final PromotionRepository promotionRepository;


    public CreateOrderCommandHandler(CreateOderValidator createOderValidator,
                                     OderRepository oderRepository,
                                     PromotionRepository promotionRepository) {
        this.createOderValidator = createOderValidator;
        this.oderRepository = oderRepository;
        this.promotionRepository = promotionRepository;
    }


    public String create(CreateOrderCommand createOrderCommand) {

        createOderValidator.validate(createOrderCommand);

        Order order = Order.create(createOrderCommand.items());

        oderRepository.save(order);
        return "order created";
    }
}
