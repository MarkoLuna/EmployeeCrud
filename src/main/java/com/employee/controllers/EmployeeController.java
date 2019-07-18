package com.employee.controllers;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.exceptions.EmployeeNotFound;
import com.employee.services.EmployeeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Log4j2
@RestController
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/employee/{page}/{size}")
    public ResponseEntity<Page<EmployeeDto>> list(@PathVariable("page") Integer page, @PathVariable("size") Integer pageSize) {
        try {
            Page<EmployeeDto> employeeDtoList = employeeService.list(page, pageSize);
            return new ResponseEntity<>(employeeDtoList, HttpStatus.OK);
        }catch (Exception e) {
            log.throwing(e);
        }
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable("id") String employeeId) {
        try {
            Optional<EmployeeDto> employee = employeeService.getEmployee(employeeId);
            return new ResponseEntity<>(employee.get(), HttpStatus.OK);
        }catch (EmployeeNotFound e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("message", e.getMessage());
            return new ResponseEntity<>(headers, HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            log.throwing(e);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
