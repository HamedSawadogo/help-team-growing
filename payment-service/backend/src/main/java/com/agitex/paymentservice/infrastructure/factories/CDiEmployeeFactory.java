package com.agitex.paymentservice.infrastructure.factories;

import com.agitex.paymentservice.domain.rh.*;
import org.springframework.stereotype.Component;


@Component
public class CDiEmployeeFactory implements EmployeeFactory {

    @Override
    public Employee create(EmployeeContext context) {
        return new CdiEmployee(context.getFirstName(),
                context.getLastName(), context.getBaseSalary());
    }

    @Override
    public EmployeeType type() {
        return EmployeeType.CDI;
    }
}
