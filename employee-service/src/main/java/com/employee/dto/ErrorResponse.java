package com.employee.dto;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            timezone = "UTC")
    private Instant timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    private Map<String, String> validationErrors;
}
