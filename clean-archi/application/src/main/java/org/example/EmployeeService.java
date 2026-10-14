package org.example;

import org.example.repositories.EmployeeRepository;
import java.math.BigDecimal;

public class EmployeeService {

    private final EmployeeFactory employeeFactory;

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeFactory employeeFactory,
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