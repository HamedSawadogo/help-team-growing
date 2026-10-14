package org.cleanarchi.domain;

import org.cleanarchi.application.CreateEmployeeCommand;
import org.cleanarchi.application.EmployeeFactory;

public class EmployeeFactoryImpl implements EmployeeFactory {

    public Employee create(CreateEmployeeCommand command) {
        return switch (command.type()) {
            case CDI -> new CDIEmployee(command.name(), command.contractStartedDate());
            case CDD -> new CDDEmployee(
                    command.name(),
                    command.contractStartedDate(),
                    command.contractEndDate());
        };
    }
}
