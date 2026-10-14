package org.example;

import org.example.factory.CreateEmployeeCommand;
import org.example.factory.EmployeeFactory;
import org.example.repositories.EmployeeRepository;
import java.math.BigDecimal;

public class EmployeeCommandHandler {

    private final EmployeeFactory employeeFactory;

    private final EmployeeRepository employeeRepository;

    public EmployeeCommandHandler(EmployeeFactory employeeFactory,
                           EmployeeRepository employeeRepository) {
        this.employeeFactory = employeeFactory;
        this.employeeRepository = employeeRepository;
    }


    public Employee create(CreateEmployeeCommand command) {
        Employee employee = employeeFactory.create(command);
        employeeRepository.save(employee);
        return employee;
    }


    public BigDecimal calculateSalary(Employee employee) {
        return employee.calculateSalary();
    }
}