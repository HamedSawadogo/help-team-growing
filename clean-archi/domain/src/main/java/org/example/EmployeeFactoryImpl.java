package org.example;


import org.example.repositories.EmployeeRepository;

import static org.example.EmployeeType.CDD;
import static org.example.EmployeeType.CDI;

public class EmployeeFactoryImpl implements EmployeeRepository {

    public Employee create(Creat command) {
        return switch (command.type()) {
            case CDI -> new CDIEmployee(command.name(), command.contractStartedDate());
            case CDD -> new CDDEmployee(
                    command.name(),
                    command.contractStartedDate(),
                    command.contractEndDate());
        };
    }
}
