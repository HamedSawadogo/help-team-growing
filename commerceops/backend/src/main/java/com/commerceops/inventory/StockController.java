package com.commerceops.inventory;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class StockController {

    private final StockLevelRepository stockLevelRepository;

    public StockController(StockLevelRepository stockLevelRepository) {
        this.stockLevelRepository = stockLevelRepository;
    }

    @GetMapping("/{productId}/stock")
    public StockLevelResponse getStock(@PathVariable Long productId) {
        return StockLevelResponse.from(stockLevelRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("No stock level for product " + productId)));
    }
}
