package com.employee.repositories;

import com.employee.entities.Employee;
import com.employee.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, String> {

    Page<Employee> findAll(Pageable pageable);
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);
}
