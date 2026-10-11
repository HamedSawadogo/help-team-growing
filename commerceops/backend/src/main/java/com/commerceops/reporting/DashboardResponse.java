package com.commerceops.reporting;

import java.math.BigDecimal;

public record DashboardResponse(
        long totalCustomers,
        long totalOrders,
        long totalInvoices,
        BigDecimal totalRevenue,
        BigDecimal totalUnpaid,
        long paymentsSucceeded,
        long paymentsFailed,
        long paymentsPending
) {
}
