package com.agitex.paymentservice.domain.rh;

public interface EmployeeFactory {

    Employee create(EmployeeContext context);

    EmployeeType type();
}