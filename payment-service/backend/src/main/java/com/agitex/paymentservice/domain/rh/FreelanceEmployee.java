package com.agitex.paymentservice.domain.rh;

public class FreelanceEmployee extends Employee implements AssignmentFees {

    public FreelanceEmployee(String firstName, String lastName) {
        super(firstName, lastName);
    }

    @Override
    public int calculateSalary() {
        return 0;
    }

    @Override
    public int calculate() {
        return 0;
    }
}
