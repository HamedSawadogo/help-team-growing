package org.cleanarchi.application;

import org.cleanarchi.domain.EmployeeType;

import java.time.LocalDate;

public record CreateEmployeeCommand(
        String name,
        LocalDate contractStartedDate,
        LocalDate contractEndDate,
        EmployeeType type) {

}
