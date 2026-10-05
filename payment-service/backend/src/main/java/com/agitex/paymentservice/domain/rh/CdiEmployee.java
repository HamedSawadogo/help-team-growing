package com.agitex.paymentservice.domain.rh;

import lombok.Getter;

@Getter
public class CdiEmployee extends Employee {

    private int baseSalary;

    public CdiEmployee(String firstName, String lastName,
                       int baseSalary) {
        super(firstName, lastName);
        this.baseSalary = baseSalary;
    }

    @Override
    public int calculateSalary() {
        return 0;
    }
}
