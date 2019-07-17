package com.employee.dto;

import lombok.Data;

@Data
public class EmployeeDto {
    private String id;
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String dateOfBirth;
    private String dateOfEmployment;
    private String status;
}
