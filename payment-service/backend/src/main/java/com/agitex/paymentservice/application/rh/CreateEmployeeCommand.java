package com.agitex.paymentservice.application.rh;

import com.agitex.paymentservice.domain.rh.EmployeeType;

public record CreateEmployeeCommand (String firstName,
                                     String lastName,
                                     int baseSalary,
                                     EmployeeType employeeType) {

}
