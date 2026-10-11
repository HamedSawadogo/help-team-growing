package com.commerceops.invoicing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Page<Invoice> findByStatus(String status, Pageable pageable);

    // Unpaid-invoices report: filters on a derived comparison with no
    // supporting composite index - the "costly search query" lab exercise
    // once invoice volume grows.
    @Query("select i from Invoice i where i.paidAmount < i.totalAmount order by i.dueDate asc")
    Page<Invoice> findUnpaid(Pageable pageable);
}
