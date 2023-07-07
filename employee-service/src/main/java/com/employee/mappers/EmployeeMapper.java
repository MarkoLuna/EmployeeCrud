package com.employee.mappers;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.entities.Employee;
import com.employee.enums.EmployeeStatus;

import jakarta.validation.constraints.NotNull;

/**
  * Mapper class between {@link EmployeeDto}, {@link Employee} and {@link EmployeeRequest}.
  */
@Service
public class EmployeeMapper {

    /**
      * Maps {@link Employee} to {@link EmployeeDto}.
      *
      * @param employee {@link Employee} to move
      * @return {@link EmployeeDto} containing the stored data.
      */
    public EmployeeDto convert(@NotNull Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .middleInitial(employee.getMiddleInitial())
                .status(employee.getStatus().name())
                .dateOfBirth(employee.getDateOfBirth())
                .dateOfEmployment(employee.getDateOfEmployment())
                .build();
    }

    /**
      * Maps a list of {@link Employee} to a list of {@link EmployeeDto}.
      *
      * @param employeeList list of {@link Employee} to move
      * @return a list of {@link EmployeeDto} containing the stored data.
      */
    public List<EmployeeDto> convert(@NotNull List<Employee> employeeList) {
        return employeeList.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public Employee convert(@NotNull EmployeeRequest emplReq) {
        Employee employee = new Employee();
        employee.setFirstName(emplReq.firstName());
        employee.setLastName(emplReq.lastName());
        employee.setMiddleInitial(emplReq.middleInitial());
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDateOfBirth(emplReq.dateOfBirth());
        employee.setDateOfEmployment(emplReq.dateOfEmployment());
        return employee;
    }

}
