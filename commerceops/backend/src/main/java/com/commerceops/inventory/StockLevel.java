package com.commerceops.inventory;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_levels")
@Getter
@Setter
@NoArgsConstructor
public class StockLevel {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false)
    private int quantityReserved;

    public int getAvailable() {
        return quantityOnHand - quantityReserved;
    }
}
