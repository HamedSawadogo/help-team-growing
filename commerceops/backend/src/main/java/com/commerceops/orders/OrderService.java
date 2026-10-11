package com.commerceops.orders;

import com.commerceops.catalog.Product;
import com.commerceops.catalog.ProductRepository;
import com.commerceops.common.ExternalNotificationClient;
import com.commerceops.customers.Customer;
import com.commerceops.customers.CustomerRepository;
import com.commerceops.inventory.StockService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final ExternalNotificationClient notificationClient;
    private final Counter ordersCreatedCounter;

    public OrderService(OrderRepository orderRepository,
                         CustomerRepository customerRepository,
                         ProductRepository productRepository,
                         StockService stockService,
                         ExternalNotificationClient notificationClient,
                         MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
        this.notificationClient = notificationClient;
        // Not named "...created": Prometheus/OpenMetrics reserves a "_created"
        // suffix for a counter's auto-generated creation-timestamp series, and
        // silently swallows it from the base name otherwise (it collapsed to
        // "commerceops_orders_total" during testing, dropping "created").
        this.ordersCreatedCounter = Counter.builder("commerceops.orders.placed")
                .description("Orders successfully created")
                .register(meterRegistry);
    }

    /**
     * Creates an order, reserves stock for each line, and notifies the
     * customer - all within a single transaction. The notification call is
     * a blocking, simulated-latency external call made from inside the
     * transaction boundary, holding the DB connection for its duration.
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + request.customerId()));

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus("CREATED");

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemRequest.productId()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getUnitPrice());
            BigDecimal lineTotal = product.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            item.setLineTotal(lineTotal);
            order.getItems().add(item);

            total = total.add(lineTotal);
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        for (OrderItemRequest itemRequest : request.items()) {
            stockService.reserve(itemRequest.productId(), itemRequest.quantity(), saved.getId());
        }

        notificationClient.sendOrderConfirmation(customer.getId(), saved.getId());

        ordersCreatedCounter.increment();
        return saved;
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public OrderResponse getDetail(Long id) {
        return OrderResponse.from(getById(id));
    }

    // The session stays open for the whole method, so mapping to
    // OrderResponse here - rather than in the controller - is what makes the
    // order.items / item.product lazy access actually run (and, on a page of
    // orders, is what turns into the N+1 query pattern instead of a hard
    // LazyInitializationException).
    @Transactional(readOnly = true)
    public Page<OrderResponse> list(String status, Pageable pageable) {
        Page<Order> orders = status != null
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);
        return orders.map(OrderResponse::from);
    }
}
