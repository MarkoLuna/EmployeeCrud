package com.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "First Name cannot be empty")
    private String firstName;

    @NotBlank(message = "Middle Initial cannot be empty")
    private String middleInitial;

    @NotBlank(message = "Last Name cannot be empty")
    private String lastName;

    @NotBlank(message = "Date Of Birth cannot be empty")
    private String dateOfBirth;

    @NotBlank(message = "Date Of Employment cannot be empty")
    private String dateOfEmployment;
}
