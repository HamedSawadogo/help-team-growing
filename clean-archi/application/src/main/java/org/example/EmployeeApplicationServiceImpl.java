package org.example;

import org.example.factory.CreateEmployeeCommand;

public class EmployeeApplicationServiceImpl implements EmployeeApplicationService {

    private final EmployeeCommandHandler employeeCommandHandler;

    public EmployeeApplicationServiceImpl(EmployeeCommandHandler employeeCommandHandler) {
        this.employeeCommandHandler = employeeCommandHandler;
    }

    @Override
    public Employee create(CreateEmployeeCommand command) {
        return employeeCommandHandler.create(command);
    }
}
