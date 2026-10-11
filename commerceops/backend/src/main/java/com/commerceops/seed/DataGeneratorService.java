package com.commerceops.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Deterministic data generator for the CommerceOps lab.
 *
 * Uses bulk JDBC batch inserts (chunked), not one row per transaction/request,
 * per the project constraint on initializing large volumes. Generated IDs are
 * recovered with a follow-up ORDER BY id SELECT, which is safe here because
 * generation is single-threaded and sequential (no concurrent writers while
 * seeding).
 */
@Service
public class DataGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(DataGeneratorService.class);
    private static final long SEED = 42L;
    private static final int BATCH_SIZE = 1000;

    private final JdbcTemplate jdbc;

    public DataGeneratorService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void reset() {
        log.info("Resetting CommerceOps data (TRUNCATE ... RESTART IDENTITY CASCADE)");
        jdbc.execute("TRUNCATE TABLE audit_events, notifications, payments, invoice_lines, invoices, " +
                "stock_movements, stock_levels, order_items, orders, contracts, products, categories, customers " +
                "RESTART IDENTITY CASCADE");
    }

    public void generate(SeedProfile profile) {
        Random random = new Random(SEED);
        log.info("Generating CommerceOps data with profile {}", profile);

        List<Long> categoryIds = generateCategories(profile, random);
        List<Long> productIds = generateProducts(profile, categoryIds, random);
        generateStockLevels(productIds, random);
        List<Long> customerIds = generateCustomers(profile, random);
        List<OrderSeed> orders = generateOrders(profile, customerIds, productIds, random);
        generateInvoicesAndPayments(orders, random);

        log.info("Data generation complete: {} customers, {} products, {} orders",
                customerIds.size(), productIds.size(), orders.size());
    }

    // ---------------------------------------------------------------- categories

    private List<Long> generateCategories(SeedProfile profile, Random random) {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < profile.categories; i++) {
            rows.add(new Object[]{"Category " + (i + 1), null});
        }
        batchInsert("INSERT INTO categories (name, parent_id) VALUES (?, ?)", rows);
        return jdbc.queryForList("SELECT id FROM categories ORDER BY id", Long.class);
    }

    // ---------------------------------------------------------------- products

    private List<Long> generateProducts(SeedProfile profile, List<Long> categoryIds, Random random) {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < profile.products; i++) {
            String sku = "SKU-" + String.format("%06d", i + 1);
            String name = "Product " + (i + 1);
            Long categoryId = categoryIds.get(random.nextInt(categoryIds.size()));
            BigDecimal price = BigDecimal.valueOf(5 + random.nextDouble() * 495).setScale(2, RoundingMode.HALF_UP);
            rows.add(new Object[]{sku, name, categoryId, price, true});
        }
        batchInsert("INSERT INTO products (sku, name, category_id, unit_price, active) VALUES (?, ?, ?, ?, ?)", rows);
        return jdbc.queryForList("SELECT id FROM products ORDER BY id", Long.class);
    }

    private void generateStockLevels(List<Long> productIds, Random random) {
        List<Object[]> rows = new ArrayList<>();
        for (Long productId : productIds) {
            int onHand = 2000 + random.nextInt(48000);
            rows.add(new Object[]{productId, onHand, 0});
        }
        batchInsert("INSERT INTO stock_levels (product_id, quantity_on_hand, quantity_reserved) VALUES (?, ?, ?)", rows);
    }

    // ---------------------------------------------------------------- customers

    private List<Long> generateCustomers(SeedProfile profile, Random random) {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < profile.customers; i++) {
            String code = "CUS-" + String.format("%06d", i + 1);
            String name = "Customer " + (i + 1);
            String email = "customer" + (i + 1) + "@example.test";
            String segment = pickSegment(random);
            rows.add(new Object[]{code, name, email, "+1555" + String.format("%07d", i), segment, true});
        }
        batchInsert("INSERT INTO customers (code, name, email, phone, segment, active) VALUES (?, ?, ?, ?, ?, ?)", rows);
        return jdbc.queryForList("SELECT id FROM customers ORDER BY id", Long.class);
    }

    private String pickSegment(Random random) {
        double r = random.nextDouble();
        if (r < 0.1) return "WHOLESALE";
        if (r < 0.3) return "VIP";
        return "STANDARD";
    }

    // ---------------------------------------------------------------- orders

    private record OrderItemSeed(long productId, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    private record OrderSeed(long orderId, long customerId, LocalDateTime orderDate,
                              BigDecimal total, List<OrderItemSeed> items) {
    }

    private List<OrderSeed> generateOrders(SeedProfile profile, List<Long> customerIds, List<Long> productIds,
                                            Random random) {
        // Weighted sampling pool: ~5% of customers are "whales" and appear
        // 10x more often, so order volume per customer is skewed, not uniform.
        List<Long> weightedCustomers = buildWeightedPool(customerIds, random, 0.05, 10);
        List<BigDecimal> productPrices = fetchProductPrices(productIds);

        List<Object[]> orderRows = new ArrayList<>();
        List<List<OrderItemSeed>> itemsPerOrder = new ArrayList<>();
        List<LocalDateTime> orderDates = new ArrayList<>();
        List<Long> orderCustomerIds = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < profile.orders; i++) {
            long customerId = weightedCustomers.get(random.nextInt(weightedCustomers.size()));
            LocalDateTime orderDate = now.minusDays(random.nextInt(540)).minusHours(random.nextInt(24));

            int itemCount = random.nextDouble() < 0.02
                    ? 15 + random.nextInt(26)   // rare, large multi-line order
                    : 1 + random.nextInt(5);

            List<OrderItemSeed> items = new ArrayList<>();
            BigDecimal orderTotal = BigDecimal.ZERO;
            for (int j = 0; j < itemCount; j++) {
                int productIndex = random.nextInt(productIds.size());
                long productId = productIds.get(productIndex);
                BigDecimal unitPrice = productPrices.get(productIndex);
                int quantity = 1 + random.nextInt(10);
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                items.add(new OrderItemSeed(productId, quantity, unitPrice, lineTotal));
                orderTotal = orderTotal.add(lineTotal);
            }

            orderRows.add(new Object[]{customerId, "CREATED", orderDate, orderTotal, orderDate});
            itemsPerOrder.add(items);
            orderDates.add(orderDate);
            orderCustomerIds.add(customerId);
        }

        batchInsert("INSERT INTO orders (customer_id, status, order_date, total_amount, created_at) " +
                "VALUES (?, ?, ?, ?, ?)", orderRows);
        List<Long> orderIds = jdbc.queryForList("SELECT id FROM orders ORDER BY id", Long.class);

        List<Object[]> itemRows = new ArrayList<>();
        List<Object[]> movementRows = new ArrayList<>();
        List<OrderSeed> orderSeeds = new ArrayList<>(orderIds.size());

        for (int i = 0; i < orderIds.size(); i++) {
            long orderId = orderIds.get(i);
            List<OrderItemSeed> items = itemsPerOrder.get(i);
            for (OrderItemSeed item : items) {
                itemRows.add(new Object[]{orderId, item.productId(), item.quantity(), item.unitPrice(), item.lineTotal()});
                movementRows.add(new Object[]{item.productId(), -item.quantity(), "RESERVATION", "ORDER", orderId, orderDates.get(i)});
            }
            BigDecimal total = items.stream().map(OrderItemSeed::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            orderSeeds.add(new OrderSeed(orderId, orderCustomerIds.get(i), orderDates.get(i), total, items));
        }

        batchInsert("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) " +
                "VALUES (?, ?, ?, ?, ?)", itemRows);
        batchInsert("INSERT INTO stock_movements (product_id, delta, movement_type, reference_type, reference_id, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)", movementRows);

        applyReservations(movementRows);

        return orderSeeds;
    }

    private List<BigDecimal> fetchProductPrices(List<Long> productIds) {
        // Loaded once into memory and indexed positionally alongside productIds;
        // fine at these volumes, would need rethinking well past LARGE.
        return jdbc.queryForList("SELECT unit_price FROM products ORDER BY id", BigDecimal.class);
    }

    private void applyReservations(List<Object[]> movementRows) {
        // Aggregate reservations per product to keep the update batch small.
        java.util.Map<Long, Integer> reservedByProduct = new java.util.HashMap<>();
        for (Object[] row : movementRows) {
            long productId = (long) row[0];
            int delta = (int) row[1];
            reservedByProduct.merge(productId, -delta, Integer::sum);
        }
        List<Object[]> updateRows = new ArrayList<>();
        for (var entry : reservedByProduct.entrySet()) {
            updateRows.add(new Object[]{entry.getValue(), entry.getKey()});
        }
        batchInsert("UPDATE stock_levels SET quantity_reserved = quantity_reserved + ? WHERE product_id = ?", updateRows);
    }

    private List<Long> buildWeightedPool(List<Long> ids, Random random, double whaleFraction, int whaleWeight) {
        List<Long> pool = new ArrayList<>();
        int whaleCount = Math.max(1, (int) (ids.size() * whaleFraction));
        for (int i = 0; i < ids.size(); i++) {
            int weight = i < whaleCount ? whaleWeight : 1;
            for (int w = 0; w < weight; w++) {
                pool.add(ids.get(i));
            }
        }
        return pool;
    }

    // ---------------------------------------------------------------- invoices + payments

    private void generateInvoicesAndPayments(List<OrderSeed> orders, Random random) {
        List<Object[]> invoiceRows = new ArrayList<>();
        List<OrderSeed> invoicedOrders = new ArrayList<>();
        int placeholderSeq = 0;

        for (OrderSeed order : orders) {
            if (random.nextDouble() >= 0.70) {
                continue; // ~30% of orders stay un-invoiced
            }
            LocalDateTime issueDate = order.orderDate().plusDays(1 + random.nextInt(3));
            LocalDate dueDate = issueDate.toLocalDate().plusDays(30);

            double coverage = random.nextDouble();
            BigDecimal paidAmount;
            String status;
            if (coverage < 0.60) {
                paidAmount = order.total();
                status = "PAID";
            } else if (coverage < 0.80) {
                double fraction = 0.2 + random.nextDouble() * 0.6;
                paidAmount = order.total().multiply(BigDecimal.valueOf(fraction)).setScale(2, RoundingMode.HALF_UP);
                status = dueDate.isBefore(LocalDate.now()) ? "OVERDUE" : "PARTIALLY_PAID";
            } else {
                paidAmount = BigDecimal.ZERO;
                status = dueDate.isBefore(LocalDate.now()) ? "OVERDUE" : "ISSUED";
            }

            invoiceRows.add(new Object[]{order.orderId(), order.customerId(), "PENDING-" + (placeholderSeq++), status,
                    issueDate, dueDate, order.total(), paidAmount, issueDate});
            invoicedOrders.add(order);
        }

        batchInsert("INSERT INTO invoices (order_id, customer_id, invoice_number, status, issue_date, due_date, " +
                "total_amount, paid_amount, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", invoiceRows);

        List<Long> invoiceIds = jdbc.queryForList("SELECT id FROM invoices ORDER BY id", Long.class);

        List<Object[]> numberUpdates = new ArrayList<>();
        List<Object[]> lineRows = new ArrayList<>();
        List<Object[]> paymentRows = new ArrayList<>();

        for (int i = 0; i < invoiceIds.size(); i++) {
            long invoiceId = invoiceIds.get(i);
            OrderSeed order = invoicedOrders.get(i);
            Object[] invoiceRow = invoiceRows.get(i);
            String status = (String) invoiceRow[3];
            BigDecimal total = (BigDecimal) invoiceRow[6];
            BigDecimal paidAmount = (BigDecimal) invoiceRow[7];

            numberUpdates.add(new Object[]{"INV-" + String.format("%07d", invoiceId), invoiceId});

            for (OrderItemSeed item : order.items()) {
                lineRows.add(new Object[]{invoiceId, "Line for product " + item.productId(),
                        item.quantity(), item.unitPrice(), item.lineTotal()});
            }

            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                paymentRows.add(new Object[]{invoiceId, "SEED-PAY-" + invoiceId, paidAmount, "SUCCEEDED",
                        "PSP-SEED-" + invoiceId, 1, order.orderDate().plusDays(2)});
            } else if (random.nextDouble() < 0.1) {
                paymentRows.add(new Object[]{invoiceId, "SEED-PAY-" + invoiceId, total, "FAILED",
                        "PSP-SEED-" + invoiceId, 1, order.orderDate().plusDays(2)});
            }
        }

        batchInsert("UPDATE invoices SET invoice_number = ? WHERE id = ?", numberUpdates);
        batchInsert("INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, line_total) " +
                "VALUES (?, ?, ?, ?, ?)", lineRows);
        batchInsert("INSERT INTO payments (invoice_id, idempotency_key, amount, status, provider_reference, " +
                "attempt_count, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)", paymentRows);
    }

    // ---------------------------------------------------------------- helpers

    private void batchInsert(String sql, List<Object[]> rows) {
        if (rows.isEmpty()) {
            return;
        }
        for (int i = 0; i < rows.size(); i += BATCH_SIZE) {
            List<Object[]> chunk = rows.subList(i, Math.min(i + BATCH_SIZE, rows.size()));
            jdbc.batchUpdate(sql, chunk);
        }
    }
}
