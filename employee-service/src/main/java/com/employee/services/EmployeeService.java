package com.employee.services;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.entities.Employee;
import com.employee.enums.EmployeeStatus;
import com.employee.exceptions.EmployeeNotFound;
import com.employee.mappers.EmployeeMapper;
import com.employee.mappers.EmployeeMapperImpl;
import com.employee.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    // -------------------------------------------------------------------------
    // Query operations
    // -------------------------------------------------------------------------

    public Page<EmployeeDto> list(Integer page, Integer sizePage) {
        Sort orders = Sort.by(Sort.Direction.DESC, "dateOfEmployment");
        Page<Employee> employeePage = employeeRepository.findByStatus(
                EmployeeStatus.ACTIVE, PageRequest.of(page, sizePage, orders));
        return employeePage.map(employeeMapper::toDto);
    }

    public EmployeeDto getEmployee(String employeeId) throws EmployeeNotFound {
        Employee employee = employeeRepository
                .findByIdAndStatus(employeeId, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new EmployeeNotFound("Unable to find the Employee"));
        return employeeMapper.toDto(employee);
    }

    // -------------------------------------------------------------------------
    // Mutation operations
    // -------------------------------------------------------------------------

    public Optional<EmployeeDto> createEmployee(EmployeeRequest req) {
        List<Employee> existing = employeeRepository.findByFirstNameAndMiddleInitialAndLastNameAndStatus(
                req.getFirstName(), req.getMiddleInitial(), req.getLastName(), EmployeeStatus.ACTIVE);

        if (!existing.isEmpty()) {
            return Optional.empty();
        }

        Employee employee = employeeMapper.toEntity(req);
        employeeRepository.save(employee);
        return Optional.of(employeeMapper.toDto(employee));
    }

    public EmployeeDto updateEmployee(String id, EmployeeRequest emplReq) throws EmployeeNotFound {
        Employee employee = employeeRepository
                .findByIdAndStatus(id, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new EmployeeNotFound("Unable to find the employee"));

        employeeMapper.updateEntity(emplReq, employee);
        employeeRepository.save(employee);
        return employeeMapper.toDto(employee);
    }

    public void deleteEmployee(String id) throws EmployeeNotFound {
        Employee employee = employeeRepository
                .findByIdAndStatus(id, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new EmployeeNotFound("Unable to find the employee"));
        employeeRepository.delete(employee);
    }

    // -------------------------------------------------------------------------
    // Validation helpers
    // -------------------------------------------------------------------------

    public Optional<String> hasValidDates(EmployeeRequest employeeReq) {
        if (!isValidDate(employeeReq.getDateOfBirth()))
            return Optional.of("Invalid Date Of Birth");

        if (!isValidDate(employeeReq.getDateOfEmployment()))
            return Optional.of("Invalid Date Of Employment");

        return Optional.empty();
    }

    private boolean isValidDate(String date) {
        try {
            if (isStringEmpty(date))
                return false;
            EmployeeMapperImpl.FORMATTER.parse(date);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    private boolean isStringEmpty(String value) {
        return Objects.isNull(value) || value.isEmpty();
    }
}
