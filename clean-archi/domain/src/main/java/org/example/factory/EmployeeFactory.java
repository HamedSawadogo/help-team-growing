package org.example.factory;

import org.example.Employee;

public interface EmployeeFactory {

    Employee create(CreateEmployeeCommand command);
}