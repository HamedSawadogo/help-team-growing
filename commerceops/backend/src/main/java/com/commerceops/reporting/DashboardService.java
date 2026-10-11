package com.commerceops.reporting;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Reporting queries run as plain aggregations against the live transactional
 * tables - no pre-computed rollups, no read replica. On the LARGE profile
 * this is deliberately the endpoint that competes with payments for
 * connections and CPU (see INCIDENT 3 in the lab notes once written up).
 */
@Service
public class DashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    private final Timer reportDurationTimer;

    public DashboardService(MeterRegistry meterRegistry) {
        this.reportDurationTimer = Timer.builder("commerceops.reports.duration")
                .description("Time to compute the dashboard summary aggregation")
                .publishPercentileHistogram()
                .register(meterRegistry);
    }

    public DashboardResponse summary() {
        return reportDurationTimer.record(this::computeSummary);
    }

    private DashboardResponse computeSummary() {
        long totalCustomers = count("select count(c) from Customer c");
        long totalOrders = count("select count(o) from Order o");
        long totalInvoices = count("select count(i) from Invoice i");
        BigDecimal totalRevenue = sum("select coalesce(sum(i.totalAmount),0) from Invoice i where i.status <> 'CANCELLED'");
        BigDecimal totalUnpaid = sum("select coalesce(sum(i.totalAmount - i.paidAmount),0) from Invoice i where i.paidAmount < i.totalAmount");
        long paymentsSucceeded = count("select count(p) from Payment p where p.status = 'SUCCEEDED'");
        long paymentsFailed = count("select count(p) from Payment p where p.status = 'FAILED'");
        long paymentsPending = count("select count(p) from Payment p where p.status = 'PENDING'");

        return new DashboardResponse(totalCustomers, totalOrders, totalInvoices, totalRevenue, totalUnpaid,
                paymentsSucceeded, paymentsFailed, paymentsPending);
    }

    private long count(String jpql) {
        return (Long) entityManager.createQuery(jpql).getSingleResult();
    }

    private BigDecimal sum(String jpql) {
        return (BigDecimal) entityManager.createQuery(jpql).getSingleResult();
    }
}
