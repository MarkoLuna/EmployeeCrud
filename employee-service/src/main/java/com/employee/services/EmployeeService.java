package com.employee.services;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class EmployeeService {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneOffset.UTC);

    @Autowired
    private EmployeeRepository employeeRepository;

    public Page<EmployeeDto> list(Integer page, Integer sizePage) {
        Sort orders = Sort.by(Sort.Direction.DESC, "dateOfEmployment");
        Page<Employee> employeeList = employeeRepository.findByStatus(EmployeeStatus.ACTIVE, PageRequest.of(page, sizePage, orders));
        return employeeList.map(this::mapEmployeeDto);
    }

    private EmployeeDto mapEmployeeDto(Employee empl) {
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setId(empl.getId());
        employeeDto.setFirstName(empl.getFirstName());
        employeeDto.setLastName(empl.getLastName());
        employeeDto.setMiddleInitial(empl.getMiddleInitial());
        employeeDto.setStatus(empl.getStatus().name());
        employeeDto.setDateOfBirth(formatDateToString(empl.getDateOfBirth()));
        employeeDto.setDateOfEmployment(formatDateToString(empl.getDateOfEmployment()));

        return employeeDto;
    }

    private Employee mapEmployeeEntity(EmployeeRequest emplReq) {
        Employee employee = new Employee();
        employee.setFirstName(emplReq.getFirstName());
        employee.setLastName(emplReq.getLastName());
        employee.setMiddleInitial(emplReq.getMiddleInitial());
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDateOfBirth(formatDateToString(emplReq.getDateOfBirth()));
        employee.setDateOfEmployment(formatDateToString(emplReq.getDateOfEmployment()));
        return employee;
    }

    private String formatDateToString(LocalDate date) {
        if(Objects.nonNull(date)) {
            return formatter.format(date);
        }
        return "";
    }

    private LocalDate formatDateToString(String date) {
        if(Objects.nonNull(date) && !date.isEmpty()) {
            return LocalDate.parse(date, formatter);
        }
        return LocalDate.now();
    }

    private boolean isValidDate(String date) {
        try {
            if(isStringEmpty(date))
                return false;
            formatter.parse(date);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    private boolean isStringEmpty(String value) {
        return Objects.isNull(value) || value.isEmpty();
    }

    public EmployeeDto getEmployee(String employeeId) throws EmployeeNotFound {
        Optional<Employee> employee = employeeRepository.findByIdAndStatus(employeeId, EmployeeStatus.ACTIVE);
        return mapEmployeeDto(employee.orElseThrow(() -> new EmployeeNotFound("Unable to find the Employee")));
    }

    public Optional<String> hasValidDates(EmployeeRequest employeeReq) {
        if(!isValidDate(employeeReq.getDateOfBirth()))
            return Optional.of("Invalid Date Of Birth");

        if(!isValidDate(employeeReq.getDateOfEmployment()))
            return Optional.of("Invalid Date Of Employment");

        return Optional.empty();
    }

    public Optional<EmployeeDto> createEmployee(EmployeeRequest req) {
        List<Employee> existanEmployee = employeeRepository.findByFirstNameAndMiddleInitialAndLastNameAndStatus(
                req.getFirstName(), req.getMiddleInitial(), req.getLastName(), EmployeeStatus.ACTIVE);

        if(!existanEmployee.isEmpty())
            return Optional.empty();

        Employee employee = mapEmployeeEntity(req);
        employeeRepository.save(employee);
        EmployeeDto employeeDto = mapEmployeeDto(employee);
        return Optional.of(employeeDto);
    }

    public EmployeeDto updateEmployee(String id, EmployeeRequest emplReq) throws EmployeeNotFound {

        Optional<Employee> existanEmployee = employeeRepository.findByIdAndStatus(id, EmployeeStatus.ACTIVE);
        Employee employee = existanEmployee.orElseThrow(() -> new EmployeeNotFound("Unable to find the employee"));

        employee.setFirstName(emplReq.getFirstName());
        employee.setLastName(emplReq.getLastName());
        employee.setMiddleInitial(emplReq.getMiddleInitial());
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDateOfBirth(formatDateToString(emplReq.getDateOfBirth()));
        employee.setDateOfEmployment(formatDateToString(emplReq.getDateOfEmployment()));

        employeeRepository.save(employee);
        return mapEmployeeDto(employee);
    }

    public void deleteEmployee(String id) throws EmployeeNotFound {
        Optional<Employee> existanEmployee = employeeRepository.findByIdAndStatus(id, EmployeeStatus.ACTIVE);
        Employee employee = existanEmployee.orElseThrow(() -> new EmployeeNotFound("Unable to find the employee"));
        employeeRepository.delete(employee);
    }
}
