package com.agitex.paymentservice.domain.repositories;

import com.agitex.paymentservice.domain.rh.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
}
