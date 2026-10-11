package com.commerceops.inventory;

public record StockLevelResponse(Long productId, int quantityOnHand, int quantityReserved, int available) {
    public static StockLevelResponse from(StockLevel level) {
        return new StockLevelResponse(level.getProductId(), level.getQuantityOnHand(),
                level.getQuantityReserved(), level.getAvailable());
    }
}
