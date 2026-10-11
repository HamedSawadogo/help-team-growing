package com.commerceops.catalog;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String categoryName,
        BigDecimal unitPrice,
        boolean active
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getSku(), p.getName(),
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getUnitPrice(), p.isActive());
    }
}
