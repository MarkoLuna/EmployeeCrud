package com.employee.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record EmployeeRequest(
        @NotBlank(message = "First Name cannot be empty")
        String firstName,

        @NotBlank(message = "Middle Initial cannot be empty")
        String middleInitial,

        @NotBlank(message = "Last Name cannot be empty")
        String lastName,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "UTC")
        LocalDate dateOfBirth,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "UTC")
        LocalDate dateOfEmployment) {

}
