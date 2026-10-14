package org.example.factory;


import org.example.Employee;

/**
 * EmployeeFactory
 */
public interface EmployeeFactory {

    Employee create(CreateEmployeeCommand command);
}