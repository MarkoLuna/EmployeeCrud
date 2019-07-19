package com.employee.controllers;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.exceptions.EmployeeNotFound;
import com.employee.exceptions.InvalidDataException;
import com.employee.services.EmployeeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private static final String MESSAGE_HEADER = "message";

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/{page}/{size}")
    public ResponseEntity<Page<EmployeeDto>> list(@PathVariable("page") Integer page, @PathVariable("size") Integer pageSize) {
        try {
            Page<EmployeeDto> employeeDtoList = employeeService.list(page, pageSize);
            return new ResponseEntity<>(employeeDtoList, HttpStatus.OK);
        }catch (Exception e) {
            log.throwing(e);
        }
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable("id") String employeeId) {
        try {
            Optional<EmployeeDto> employee = employeeService.getEmployee(employeeId);
            return new ResponseEntity<>(employee.get(), HttpStatus.OK);
        }catch (EmployeeNotFound e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(MESSAGE_HEADER, e.getMessage());
            return new ResponseEntity<>(headers, HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            log.throwing(e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping()
    public ResponseEntity<EmployeeDto> saveEmployee(@Valid @RequestBody EmployeeRequest request) {
        try {
            Optional<String> invalidError = employeeService.hasValidDates(request);
            if(invalidError.isPresent())
                throw new InvalidDataException(invalidError.get());

            Optional<EmployeeDto> employeeOpt = employeeService.createEmployee(request);
            return new ResponseEntity<>(employeeOpt.orElseThrow(() -> new InvalidDataException("Employee already exists ..")), HttpStatus.OK);
        }catch (InvalidDataException e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(MESSAGE_HEADER, e.getMessage());
            return new ResponseEntity<>(headers, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.throwing(e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable("id") String employeeId, @Valid @RequestBody EmployeeRequest request) {
        try {
            EmployeeDto employee = employeeService.updateEmployee(employeeId, request);
            return new ResponseEntity<>(employee, HttpStatus.OK);
        } catch (EmployeeNotFound e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(MESSAGE_HEADER, e.getMessage());
            return new ResponseEntity<>(headers, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.throwing(e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable("id") String employeeId) {
        try {
            employeeService.deleteEmployee(employeeId);
            return new ResponseEntity<>("Employee Deleted", HttpStatus.OK);
        } catch (EmployeeNotFound e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(MESSAGE_HEADER, e.getMessage());
            return new ResponseEntity<>(headers, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.throwing(e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
