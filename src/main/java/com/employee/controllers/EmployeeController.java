package com.employee.controllers;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.services.EmployeeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/employee")
    public ResponseEntity<List<EmployeeDto>> list() {
        return null;
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<EmployeeDto> getEmployee() {
        return null;
    }

    @PostMapping("/employee")
    public ResponseEntity<String> saveEmployee(@RequestBody EmployeeRequest request) {
        return null;
    }

    @DeleteMapping("/employee/{id}")
    public ResponseEntity<String> deleteEmployee() {
        return null;
    }
}
