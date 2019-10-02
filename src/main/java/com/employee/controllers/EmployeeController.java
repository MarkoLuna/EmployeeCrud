package com.employee.controllers;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.exceptions.EmployeeNotFound;
import com.employee.exceptions.InvalidDataException;
import com.employee.services.EmployeeService;
import io.swagger.annotations.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping(value = "/employees", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(description = "Set of endpoints for Creating, Retrieving, Updating and Deleting of Employees.",
        authorizations = { @Authorization("Bearer ...") })
public class EmployeeController {

    private static final String MESSAGE_HEADER = "message";

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/{page}/{size}")
    @ApiOperation("Returns list of all Employees in the system.")
    public ResponseEntity<Page<EmployeeDto>> list(@ApiParam(required = true, name = "page", value = "Page number of the employee list to be obtained. Cannot be empty.")
                                                      @PathVariable("page") Integer page,
                                                  @ApiParam(required = true, name = "size", value = "Page size of the employee list to be obtained. Cannot be empty.")
                                                  @PathVariable("size") Integer pageSize) {
        try {
            Page<EmployeeDto> employeeDtoList = employeeService.list(page, pageSize);
            return new ResponseEntity<>(employeeDtoList, HttpStatus.OK);
        }catch (Exception e) {
            log.throwing(e);
        }
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/{id}")
    @ApiOperation("Returns a specific employee by their identifier. 404 if does not exist.")
    public ResponseEntity<EmployeeDto> getEmployee(@ApiParam(required = true,  name = "id", value = "Id of the employee to be obtained. Cannot be empty.")
                                                       @PathVariable("id") String employeeId) {
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
    @ApiOperation("Creates a new employee.")
    public ResponseEntity<EmployeeDto> saveEmployee(
            @ApiParam("Employee information for a new employee to be created.") @Valid @RequestBody EmployeeRequest request) {
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
    @ApiOperation("Deletes a employee from the system. 404 if the employee's identifier is not found.")
    public ResponseEntity<String> deleteEmployee(@ApiParam(required = true, name = "id", value =  "Id of the employee to be deleted. Cannot be empty.")
                                                     @PathVariable("id") String employeeId) {
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
