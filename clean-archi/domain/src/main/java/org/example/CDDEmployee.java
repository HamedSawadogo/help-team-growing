package org.example;


import java.math.BigDecimal;
import java.time.LocalDate;


public class CDDEmployee implements Employee {
    private String name;
    private LocalDate contractStartedDate;
    private LocalDate contractEndDate;

    public CDDEmployee(String name,
                       LocalDate contractStartedDate,
                       LocalDate contractEndDate) {
        this.name = name;
        this.contractStartedDate = contractStartedDate;
        this.contractEndDate = contractEndDate;
    }

    @Override
    public BigDecimal calculateSalary() {
        return new BigDecimal("350000");
    }
}
