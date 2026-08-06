package com.authserver.exception;

import com.authserver.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_HEADER = "iam-error";

    @ExceptionHandler(UserManagementException.class)
    public ResponseEntity<ErrorResponse> handleUserManagementException(UserManagementException ex, WebRequest request) {
        log.error("User Management Exception: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), ex.getStatus(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.error("Access Denied Exception: {}", ex.getMessage());
        return buildErrorResponse("Access Denied: You do not have permission to perform this operation", HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
        log.error("Unhandled Exception: ", ex);
        return buildErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status, WebRequest request) {
        String path = "";
        if (request instanceof ServletWebRequest servletWebRequest) {
            path = servletWebRequest.getRequest().getRequestURI();
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(ERROR_HEADER, message);

        return new ResponseEntity<>(errorResponse, headers, status);
    }
}
