package com.employee.dto;

import lombok.Builder;

@Builder
public record EmployeeDto(
        String id,
        String firstName,
        String middleInitial,
        String lastName,
        String dateOfBirth,
        String dateOfEmployment,
        String status) {
}
