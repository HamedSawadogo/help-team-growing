package com.agitex.paymentservice.application.rh;

import com.agitex.paymentservice.domain.repositories.EmployeeRepository;
import com.agitex.paymentservice.domain.rh.Employee;
import com.agitex.paymentservice.domain.rh.EmployeeContext;
import com.agitex.paymentservice.domain.rh.EmployeeFactory;

public class CreateEmployeeApplicationService {

    private final EmployeeRegistry employeeRegistry;
    private final EmployeeRepository employeeRepository;

    public CreateEmployeeApplicationService(EmployeeRegistry employeeRegistry,
                                            EmployeeRepository employeeRepository) {
        this.employeeRegistry = employeeRegistry;
        this.employeeRepository = employeeRepository;
    }

    public Employee create(CreateEmployeeCommand command) {
       final EmployeeContext employeeContext = new EmployeeContext(command.firstName(), command.lastName(), command.baseSalary());

       final EmployeeFactory employeeFactory = employeeRegistry.get(command.employeeType());
       final Employee employee = employeeFactory.create(employeeContext);

       return employeeRepository.save(employee);
    }
}
