package com.commerceops.invoicing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        String customerName,
        String status,
        LocalDateTime issueDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        LocalDateTime orderDate
) {
    public static InvoiceResponse from(Invoice invoice) {
        LocalDateTime orderDate = invoice.getOrder() != null ? invoice.getOrder().getOrderDate() : null;
        return new InvoiceResponse(invoice.getId(), invoice.getInvoiceNumber(), invoice.getCustomer().getName(),
                invoice.getStatus(), invoice.getIssueDate(), invoice.getDueDate(),
                invoice.getTotalAmount(), invoice.getPaidAmount(), orderDate);
    }
}
