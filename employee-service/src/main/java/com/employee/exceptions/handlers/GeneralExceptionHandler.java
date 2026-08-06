package com.employee.exceptions.handlers;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.employee.dto.ErrorResponse;
import com.employee.exceptions.EmployeeNotFound;
import com.employee.exceptions.InvalidDataException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {
    
    private static final String MESSAGE_HEADER = "app-context-error";

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Unhandled Exception ({}): {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return buildErrorResponse("An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(EmployeeNotFound.class)
    public final ResponseEntity<ErrorResponse> handleEmployeeNotFoundException(EmployeeNotFound ex, WebRequest request) {
        log.error("Employee Not Found: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }
    
    @ExceptionHandler(InvalidDataException.class)
    public final ResponseEntity<ErrorResponse> handleInvalidDataException(InvalidDataException ex, WebRequest request) {
        log.error("Invalid Data Error: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.error("Access Denied: {}", ex.getMessage());
        return buildErrorResponse("Access Denied: " + ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        log.error("Constraint Violation Error: {}", ex.getMessage());
        Map<String, String> validationErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            validationErrors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation Failed")
                .path(getPath(request))
                .validationErrors(validationErrors)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, 
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        
        log.error("Method Argument Not Valid: {}", ex.getMessage());
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            if (error instanceof FieldError fieldError) {
                validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                validationErrors.put(error.getObjectName(), error.getDefaultMessage());
            }
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(HttpStatus.valueOf(status.value()).getReasonPhrase())
                .message("Validation Failed")
                .path(getPath(request))
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, 
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("HTTP Message Not Readable: {}", ex.getMessage());
        return new ResponseEntity<>(buildErrorResponse("Malformed JSON request or invalid data types", HttpStatus.BAD_REQUEST, request).getBody(), status);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage(), HttpStatus.valueOf(status.value()), request).getBody(), status);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage(), HttpStatus.valueOf(status.value()), request).getBody(), status);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status, WebRequest request) {
        String path = getPath(request);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(MESSAGE_HEADER, message);

        return new ResponseEntity<>(errorResponse, headers, status);
    }

    private String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return "";
    }
}
