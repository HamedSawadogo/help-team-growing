package com.agitex.paymentservice.domain.rh;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class Employee {
    protected String firstName;
    protected String lastName;

    public Employee(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public abstract int calculateSalary();
}
