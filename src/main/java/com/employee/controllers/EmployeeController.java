package com.employee.controllers;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.exceptions.InvalidDataException;
import com.employee.services.EmployeeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping(value = "/employees", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(description = "Set of endpoints for Creating, Retrieving, Updating and Deleting of Employees.")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/{page}/{size}")
    @ApiOperation("Returns list of all Employees in the system.")
    public ResponseEntity<Page<EmployeeDto>> list(@ApiParam(required = true, name = "page", value = "Page number of the employee list to be obtained. Cannot be empty.")
                                                      @PathVariable("page") Integer page,
                                                  @ApiParam(required = true, name = "size", value = "Page size of the employee list to be obtained. Cannot be empty.")
                                                  @PathVariable("size") Integer pageSize) {
    	
        Page<EmployeeDto> employeeDtoList = employeeService.list(page, pageSize);
        return new ResponseEntity<>(employeeDtoList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @ApiOperation("Returns a specific employee by their identifier. 404 if does not exist.")
    public ResponseEntity<EmployeeDto> getEmployee(@ApiParam(required = true,  name = "id", value = "Id of the employee to be obtained. Cannot be empty.")
                                                       @PathVariable("id") String employeeId) {

        EmployeeDto employee = employeeService.getEmployee(employeeId);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @PostMapping()
    @ApiOperation("Creates a new employee.")
    public ResponseEntity<EmployeeDto> saveEmployee(
            @ApiParam("Employee information for a new employee to be created.") @Valid @RequestBody EmployeeRequest request) {
        
        Optional<String> invalidError = employeeService.hasValidDates(request);
        if(invalidError.isPresent())
            throw new InvalidDataException(invalidError.get());

        EmployeeDto employee = employeeService.createEmployee(request)
                .orElseThrow(() -> new InvalidDataException("Employee already exists .."));
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable("id") String employeeId, @Valid @RequestBody EmployeeRequest request) {

        EmployeeDto employee = employeeService.updateEmployee(employeeId, request);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("Deletes a employee from the system. 404 if the employee's identifier is not found.")
    public ResponseEntity<String> deleteEmployee(@ApiParam(required = true, name = "id", value =  "Id of the employee to be deleted. Cannot be empty.")
                                                     @PathVariable("id") String employeeId) {

        employeeService.deleteEmployee(employeeId);
        return new ResponseEntity<>("Employee Deleted", HttpStatus.OK);
    }
}
