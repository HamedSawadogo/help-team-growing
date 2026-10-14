package org.cleanarchi.domain;

import java.math.BigDecimal;
import java.time.LocalDate;


class CDIEmployee implements Employee {
    private String name;
    private LocalDate contractStartedDate;

    public CDIEmployee(String name, LocalDate contractStartedDate) {
        this.name = name;
        this.contractStartedDate = contractStartedDate;
    }

    private void validate(String name, LocalDate contractStartedDate) {
        if (name.startsWith("W")) {
            throw new RuntimeException("Employee Name invalid");
        }
        if (contractStartedDate.isAfter(LocalDate.of(2024, 2, 3))) {
            throw new RuntimeException("Fraude");
        }
    }

    @Override
    public BigDecimal calculateSalary() {
        return new BigDecimal("500000");
    }
}
