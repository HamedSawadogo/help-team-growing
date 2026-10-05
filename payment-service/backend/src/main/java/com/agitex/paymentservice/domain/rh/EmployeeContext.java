package com.agitex.paymentservice.domain.rh;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeContext {
    private String firstName;
    private String lastName;
    private int baseSalary;
}
