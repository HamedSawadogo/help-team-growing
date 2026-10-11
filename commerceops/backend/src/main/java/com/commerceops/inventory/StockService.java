package com.commerceops.inventory;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;

    public StockService(StockLevelRepository stockLevelRepository,
                         StockMovementRepository stockMovementRepository) {
        this.stockLevelRepository = stockLevelRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    /**
     * Reserves stock for an order line.
     *
     * Plain read-check-write: no pessimistic lock, no optimistic version check.
     * Under concurrent calls for the same product this can over-reserve -
     * that race is intentional and is the subject of the concurrency lab.
     */
    @Transactional
    public void reserve(Long productId, int quantity, Long orderId) {
        StockLevel level = stockLevelRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("No stock level for product " + productId));

        if (level.getAvailable() < quantity) {
            throw new InsufficientStockException(productId, quantity, level.getAvailable());
        }

        level.setQuantityReserved(level.getQuantityReserved() + quantity);
        stockLevelRepository.save(level);

        StockMovement movement = new StockMovement();
        movement.setProductId(productId);
        movement.setDelta(-quantity);
        movement.setMovementType("RESERVATION");
        movement.setReferenceType("ORDER");
        movement.setReferenceId(orderId);
        stockMovementRepository.save(movement);
    }

    @Transactional
    public void release(Long productId, int quantity, Long orderId) {
        StockLevel level = stockLevelRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("No stock level for product " + productId));

        level.setQuantityReserved(Math.max(0, level.getQuantityReserved() - quantity));
        stockLevelRepository.save(level);

        StockMovement movement = new StockMovement();
        movement.setProductId(productId);
        movement.setDelta(quantity);
        movement.setMovementType("RELEASE");
        movement.setReferenceType("ORDER");
        movement.setReferenceId(orderId);
        stockMovementRepository.save(movement);
    }
}
