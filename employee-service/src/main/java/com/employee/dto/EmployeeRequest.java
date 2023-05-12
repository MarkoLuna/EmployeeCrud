package com.employee.dto;

import lombok.Builder;

import javax.validation.constraints.NotBlank;

@Builder
public record EmployeeRequest(
        @NotBlank(message = "First Name cannot be empty")
        String firstName,

        @NotBlank(message = "Middle Initial cannot be empty")
        String middleInitial,

        @NotBlank(message = "Last Name cannot be empty")
        String lastName,

        @NotBlank(message = "Date Of Birth cannot be empty")
        String dateOfBirth,

        @NotBlank(message = "Date Of Employment cannot be empty")
        String dateOfEmployment) {

}
