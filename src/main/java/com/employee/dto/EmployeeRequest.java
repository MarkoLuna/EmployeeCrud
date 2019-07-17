package com.employee.dto;

import lombok.Data;

@Data
public class EmployeeRequest {
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String dateOfBirth;
    private String dateOfEmployment;
    private String status;
}
