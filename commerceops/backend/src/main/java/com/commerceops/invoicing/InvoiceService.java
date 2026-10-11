package com.commerceops.invoicing;

import com.commerceops.orders.Order;
import com.commerceops.orders.OrderItem;
import com.commerceops.orders.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, OrderRepository orderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Invoice issueFromOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setCustomer(order.getCustomer());
        invoice.setTotalAmount(order.getTotalAmount());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        // placeholder, replaced once the id is known
        invoice.setInvoiceNumber("PENDING");

        for (OrderItem item : order.getItems()) {
            InvoiceLine line = new InvoiceLine();
            line.setInvoice(invoice);
            line.setDescription(item.getProduct().getName());
            line.setQuantity(item.getQuantity());
            line.setUnitPrice(item.getUnitPrice());
            line.setLineTotal(item.getLineTotal());
            invoice.getLines().add(line);
        }

        Invoice saved = invoiceRepository.save(invoice);
        saved.setInvoiceNumber("INV-" + String.format("%07d", saved.getId()));
        order.setStatus("INVOICED");
        return invoiceRepository.save(saved);
    }

    @Transactional
    public InvoiceDetailResponse issueFromOrderDetail(Long orderId) {
        return InvoiceDetailResponse.from(issueFromOrder(orderId));
    }

    public Invoice getById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + id));
    }

    @Transactional(readOnly = true)
    public InvoiceDetailResponse getDetail(Long id) {
        return InvoiceDetailResponse.from(getById(id));
    }

    // See OrderService.list: mapping has to happen while the session from
    // the paged query is still open, because Invoice.order is LAZY and the
    // list DTO reads order.getOrderDate().
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> list(String status, Pageable pageable) {
        Page<Invoice> invoices = status != null
                ? invoiceRepository.findByStatus(status, pageable)
                : invoiceRepository.findAll(pageable);
        return invoices.map(InvoiceResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> unpaid(Pageable pageable) {
        return invoiceRepository.findUnpaid(pageable).map(InvoiceResponse::from);
    }
}
