package com.employee.entities;

import com.employee.enums.EmployeeStatus;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@ToString
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String firstName;
    private String middleInitial;
    private String lastName;
    private LocalDate dateOfBirth;
    private LocalDate dateOfEmployment;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private EmployeeStatus status;
}
