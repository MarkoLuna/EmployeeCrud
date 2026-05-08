package com.employee.mappers;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.entities.Employee;
import com.employee.enums.EmployeeStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Default implementation of {@link EmployeeMapper}.
 * Handles all date formatting and field mapping between layers.
 */
@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneOffset.UTC);

    @Override
    public EmployeeDto toDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setMiddleInitial(employee.getMiddleInitial());
        dto.setStatus(employee.getStatus().name());
        dto.setDateOfBirth(localDateToString(employee.getDateOfBirth()));
        dto.setDateOfEmployment(localDateToString(employee.getDateOfEmployment()));
        return dto;
    }

    @Override
    public Employee toEntity(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setMiddleInitial(request.getMiddleInitial());
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDateOfBirth(stringToLocalDate(request.getDateOfBirth()));
        employee.setDateOfEmployment(stringToLocalDate(request.getDateOfEmployment()));
        return employee;
    }

    @Override
    public void updateEntity(EmployeeRequest request, Employee employee) {
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setMiddleInitial(request.getMiddleInitial());
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDateOfBirth(stringToLocalDate(request.getDateOfBirth()));
        employee.setDateOfEmployment(stringToLocalDate(request.getDateOfEmployment()));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private String localDateToString(LocalDate date) {
        if (Objects.nonNull(date)) {
            return FORMATTER.format(date);
        }
        return "";
    }

    private LocalDate stringToLocalDate(String date) {
        if (Objects.nonNull(date) && !date.isEmpty()) {
            return LocalDate.parse(date, FORMATTER);
        }
        return LocalDate.now();
    }
}
