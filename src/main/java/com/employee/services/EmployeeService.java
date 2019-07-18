package com.employee.services;

import com.employee.dto.EmployeeDto;
import com.employee.entities.Employee;
import com.employee.enums.EmployeeStatus;
import com.employee.exceptions.EmployeeNotFound;
import com.employee.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public Page<EmployeeDto> list(Integer page, Integer sizePage) {
        Sort orders = new Sort(Sort.Direction.DESC, "dateOfEmployment");
        Page<Employee> employeeList = employeeRepository.findByStatus(EmployeeStatus.ACTIVE, PageRequest.of(page, sizePage, orders));
        return employeeList.map(this::mapEmployeeDto);
    }

    private EmployeeDto mapEmployeeDto(Employee empl) {
        return EmployeeDto.builder()
                .id(empl.getId())
                .firstName(empl.getFirstName())
                .lastName(empl.getLastName())
                .middleInitial(empl.getMiddleInitial())
                .status(empl.getStatus().name())
                .dateOfBirth(formatDateToString(empl.getDateOfBirth()))
                .dateOfEmployment(formatDateToString(empl.getDateOfEmployment()))
                .build();
    }

    private String formatDateToString(LocalDate date) {
        if(Objects.nonNull(date)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return formatter.withZone(ZoneOffset.UTC).format(date);
        }
        return "";
    }

    public Optional<EmployeeDto> getEmployee(String employeeId) throws EmployeeNotFound {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        EmployeeDto employeeDto = mapEmployeeDto(employee.orElseThrow(() -> new EmployeeNotFound("Unable to find the Employee")));
        return Optional.of(employeeDto);
    }
}
