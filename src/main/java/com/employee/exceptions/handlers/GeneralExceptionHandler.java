package com.employee.exceptions.handlers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.employee.exceptions.EmployeeNotFound;
import com.employee.exceptions.InvalidDataException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@ControllerAdvice
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {
	
	private static final String MESSAGE_HEADER = "message";
	
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
    	log.throwing(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
 
    @ExceptionHandler(EmployeeNotFound.class)
    public final ResponseEntity<String> handleEmployeeNotFoundException(EmployeeNotFound ex,
                                                WebRequest request) {
    	log.throwing(ex);

        HttpHeaders headers = new HttpHeaders();
        headers.add(MESSAGE_HEADER, ex.getMessage());

        return ResponseEntity.notFound().headers(headers).build();
    }
    
    @ExceptionHandler(InvalidDataException.class)
    public final ResponseEntity<String> handleEmployeeNotFoundException(InvalidDataException ex,
    		WebRequest request) {
    	log.throwing(ex);
    	
    	HttpHeaders headers = new HttpHeaders();
        headers.add(MESSAGE_HEADER, ex.getMessage());

        return ResponseEntity.badRequest().headers(headers).build();
    }

}
