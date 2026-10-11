package com.commerceops.customers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Offset pagination over a potentially large, unindexed free-text filter:
    // deliberately kept simple for the MEDIUM profile, becomes the pagination
    // lab exercise once the table grows (LARGE profile / deep pages).
    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
