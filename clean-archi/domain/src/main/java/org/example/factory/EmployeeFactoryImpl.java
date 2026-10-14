package org.example.factory;


import org.example.CDDEmployee;
import org.example.CDIEmployee;
import org.example.Employee;
import org.example.EmployeeType;

public class EmployeeFactoryImpl implements EmployeeFactory {

    public Employee create(CreateEmployeeCommand command) {
        return switch (command.type()) {
            case EmployeeType.CDI -> new CDIEmployee(command.name(), command.contractStartedDate());
            case EmployeeType.CDD -> new CDDEmployee(
                    command.name(),
                    command.contractStartedDate(),
                    command.contractEndDate());
        };
    }
}
