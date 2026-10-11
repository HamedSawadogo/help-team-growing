package com.commerceops.invoicing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record InvoiceDetailResponse(
        Long id,
        String invoiceNumber,
        Long customerId,
        String customerName,
        String status,
        LocalDateTime issueDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal remainingDue,
        List<LineResponse> lines
) {
    public record LineResponse(String description, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    public static InvoiceDetailResponse from(Invoice invoice) {
        List<LineResponse> lines = invoice.getLines().stream()
                .map(l -> new LineResponse(l.getDescription(), l.getQuantity(), l.getUnitPrice(), l.getLineTotal()))
                .collect(Collectors.toList());
        return new InvoiceDetailResponse(invoice.getId(), invoice.getInvoiceNumber(),
                invoice.getCustomer().getId(), invoice.getCustomer().getName(), invoice.getStatus(),
                invoice.getIssueDate(), invoice.getDueDate(), invoice.getTotalAmount(), invoice.getPaidAmount(),
                invoice.getRemainingDue(), lines);
    }
}
