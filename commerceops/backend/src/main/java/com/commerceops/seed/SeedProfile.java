package com.commerceops.seed;

public enum SeedProfile {

    SMALL(500, 100, 20, 1000),
    MEDIUM(5000, 1000, 40, 20000),
    LARGE(20000, 5000, 60, 50000);

    public final int customers;
    public final int products;
    public final int categories;
    public final int orders;

    SeedProfile(int customers, int products, int categories, int orders) {
        this.customers = customers;
        this.products = products;
        this.categories = categories;
        this.orders = orders;
    }
}
