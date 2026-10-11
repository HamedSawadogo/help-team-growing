package com.commerceops.customers;

public record CustomerResponse(
        Long id,
        String code,
        String name,
        String email,
        String phone,
        String segment,
        boolean active
) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getId(), c.getCode(), c.getName(), c.getEmail(),
                c.getPhone(), c.getSegment(), c.isActive());
    }
}
