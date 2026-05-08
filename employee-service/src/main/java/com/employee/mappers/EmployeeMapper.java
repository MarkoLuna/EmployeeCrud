package com.employee.mappers;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.entities.Employee;

/**
 * Contract for mapping between {@link Employee} entities and their DTO representations.
 */
public interface EmployeeMapper {

    /**
     * Maps an {@link Employee} entity to an {@link EmployeeDto}.
     *
     * @param employee the source entity
     * @return the mapped DTO
     */
    EmployeeDto toDto(Employee employee);

    /**
     * Maps an {@link EmployeeRequest} to a new {@link Employee} entity.
     *
     * @param request the incoming creation request
     * @return a new entity ready to be persisted
     */
    Employee toEntity(EmployeeRequest request);

    /**
     * Updates an existing {@link Employee} entity with values from an {@link EmployeeRequest}.
     *
     * @param request  the source of updated values
     * @param employee the entity to be updated (mutated in place)
     */
    void updateEntity(EmployeeRequest request, Employee employee);
}
