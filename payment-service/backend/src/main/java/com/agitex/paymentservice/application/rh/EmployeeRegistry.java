package com.agitex.paymentservice.application.rh;

import com.agitex.paymentservice.domain.rh.EmployeeFactory;
import com.agitex.paymentservice.domain.rh.EmployeeType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EmployeeRegistry {
    private final Map<EmployeeType, EmployeeFactory> employeeTypeEmployeeFactoryMap;

    public EmployeeRegistry(List<EmployeeFactory> employeeFactories) {
        this.employeeTypeEmployeeFactoryMap = employeeFactories.stream()
                .collect(Collectors.toMap(EmployeeFactory::type, Function.identity()));
    }

    public EmployeeFactory get(EmployeeType type) {
        return Optional.ofNullable(employeeTypeEmployeeFactoryMap.get(type)).orElseThrow();
    }
}
