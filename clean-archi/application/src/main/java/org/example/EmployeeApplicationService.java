package org.example;

import org.example.factory.CreateEmployeeCommand;

public interface EmployeeApplicationService {

    Employee create(CreateEmployeeCommand createEmployeeCommand);
}
