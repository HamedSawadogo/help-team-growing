package org.cleanarchi.application;

import org.cleanarchi.domain.Employee;

/**
 * EmployeeFactory
 */
public interface EmployeeFactory {

    Employee create(CreateEmployeeCommand command);
}