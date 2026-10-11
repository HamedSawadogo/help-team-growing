package com.commerceops.invoicing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public Page<InvoiceResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return invoiceService.list(status, PageRequest.of(page, size));
    }

    @GetMapping("/unpaid")
    public Page<InvoiceResponse> unpaid(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return invoiceService.unpaid(PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public InvoiceDetailResponse getById(@PathVariable Long id) {
        return invoiceService.getDetail(id);
    }

    @PostMapping("/from-order/{orderId}")
    public InvoiceDetailResponse issue(@PathVariable Long orderId) {
        return invoiceService.issueFromOrderDetail(orderId);
    }
}
