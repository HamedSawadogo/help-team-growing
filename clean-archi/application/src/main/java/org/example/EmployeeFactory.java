package org.example;


/**
 * EmployeeFactory
 */
public interface EmployeeFactory {

    Employee create(CreateEmployeeCommand command);
}