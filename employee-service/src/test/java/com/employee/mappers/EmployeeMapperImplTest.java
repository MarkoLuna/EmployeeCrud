package com.employee.mappers;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.entities.Employee;
import com.employee.enums.EmployeeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

@DisplayName("EmployeeMapperImpl")
class EmployeeMapperImplTest {

    private static final String DATE_STRING    = "17-09-2012";
    private static final LocalDate DATE_LOCAL  = LocalDate.of(2012, 9, 17);
    private static final String FIRST_NAME     = "John";
    private static final String MIDDLE_INITIAL = "A";
    private static final String LAST_NAME      = "Doe";
    private static final String EMPLOYEE_ID    = "abc-123";

    private EmployeeMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new EmployeeMapperImpl();
    }

    // =========================================================================
    // toDto
    // =========================================================================
    @Nested
    @DisplayName("toDto()")
    class ToDtoTests {

        @Test
        @DisplayName("maps all fields from Employee to EmployeeDto")
        void mapsAllFields() {
            Employee employee = buildEmployee(EMPLOYEE_ID, DATE_LOCAL, DATE_LOCAL, EmployeeStatus.ACTIVE);

            EmployeeDto dto = mapper.toDto(employee);

            assertThat(dto)
                    .returns(EMPLOYEE_ID,                    from(EmployeeDto::getId))
                    .returns(FIRST_NAME,                     from(EmployeeDto::getFirstName))
                    .returns(MIDDLE_INITIAL,                 from(EmployeeDto::getMiddleInitial))
                    .returns(LAST_NAME,                      from(EmployeeDto::getLastName))
                    .returns(EmployeeStatus.ACTIVE.name(),   from(EmployeeDto::getStatus))
                    .returns(DATE_STRING,                    from(EmployeeDto::getDateOfBirth))
                    .returns(DATE_STRING,                    from(EmployeeDto::getDateOfEmployment));
        }

        @Test
        @DisplayName("returns empty string for null dateOfBirth")
        void nullDateOfBirthBecomesEmptyString() {
            Employee employee = buildEmployee(EMPLOYEE_ID, null, DATE_LOCAL, EmployeeStatus.ACTIVE);

            EmployeeDto dto = mapper.toDto(employee);

            assertThat(dto.getDateOfBirth()).isEmpty();
        }

        @Test
        @DisplayName("returns empty string for null dateOfEmployment")
        void nullDateOfEmploymentBecomesEmptyString() {
            Employee employee = buildEmployee(EMPLOYEE_ID, DATE_LOCAL, null, EmployeeStatus.ACTIVE);

            EmployeeDto dto = mapper.toDto(employee);

            assertThat(dto.getDateOfEmployment()).isEmpty();
        }

        @Test
        @DisplayName("maps INACTIVE status correctly")
        void mapsInactiveStatus() {
            Employee employee = buildEmployee(EMPLOYEE_ID, DATE_LOCAL, DATE_LOCAL, EmployeeStatus.INACTIVE);

            EmployeeDto dto = mapper.toDto(employee);

            assertThat(dto.getStatus()).isEqualTo(EmployeeStatus.INACTIVE.name());
        }
    }

    // =========================================================================
    // toEntity
    // =========================================================================
    @Nested
    @DisplayName("toEntity()")
    class ToEntityTests {

        @Test
        @DisplayName("maps all fields from EmployeeRequest to Employee entity")
        void mapsAllFields() {
            EmployeeRequest request = buildRequest(DATE_STRING, DATE_STRING);

            Employee entity = mapper.toEntity(request);

            assertThat(entity)
                    .returns(FIRST_NAME,           from(Employee::getFirstName))
                    .returns(MIDDLE_INITIAL,        from(Employee::getMiddleInitial))
                    .returns(LAST_NAME,             from(Employee::getLastName))
                    .returns(EmployeeStatus.ACTIVE, from(Employee::getStatus))
                    .returns(DATE_LOCAL,            from(Employee::getDateOfBirth))
                    .returns(DATE_LOCAL,            from(Employee::getDateOfEmployment));
        }

        @Test
        @DisplayName("always sets status to ACTIVE regardless of request content")
        void alwaysSetsStatusToActive() {
            EmployeeRequest request = buildRequest(DATE_STRING, DATE_STRING);

            Employee entity = mapper.toEntity(request);

            assertThat(entity.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        }

        @Test
        @DisplayName("does not pre-assign an id (persisted by JPA)")
        void idIsNotSetByMapper() {
            EmployeeRequest request = buildRequest(DATE_STRING, DATE_STRING);

            Employee entity = mapper.toEntity(request);

            assertThat(entity.getId()).isNull();
        }

        @Test
        @DisplayName("falls back to today when dateOfBirth is null")
        void nullDateOfBirthFallsBackToToday() {
            EmployeeRequest request = buildRequest(null, DATE_STRING);

            Employee entity = mapper.toEntity(request);

            assertThat(entity.getDateOfBirth()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("falls back to today when dateOfBirth is empty string")
        void emptyDateOfBirthFallsBackToToday() {
            EmployeeRequest request = buildRequest("", DATE_STRING);

            Employee entity = mapper.toEntity(request);

            assertThat(entity.getDateOfBirth()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("falls back to today when dateOfEmployment is null")
        void nullDateOfEmploymentFallsBackToToday() {
            EmployeeRequest request = buildRequest(DATE_STRING, null);

            Employee entity = mapper.toEntity(request);

            assertThat(entity.getDateOfEmployment()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("falls back to today when dateOfEmployment is empty string")
        void emptyDateOfEmploymentFallsBackToToday() {
            EmployeeRequest request = buildRequest(DATE_STRING, "");

            Employee entity = mapper.toEntity(request);

            assertThat(entity.getDateOfEmployment()).isEqualTo(LocalDate.now());
        }
    }

    // =========================================================================
    // updateEntity
    // =========================================================================
    @Nested
    @DisplayName("updateEntity()")
    class UpdateEntityTests {

        @Test
        @DisplayName("overwrites all mutable fields on the existing entity")
        void overwritesAllFields() {
            Employee existing = buildEmployee(EMPLOYEE_ID, DATE_LOCAL, DATE_LOCAL, EmployeeStatus.ACTIVE);

            LocalDate newDate   = LocalDate.of(1990, 1, 15);
            String    newDateStr = EmployeeMapperImpl.FORMATTER.format(newDate);
            EmployeeRequest request = new EmployeeRequest("Jane", "B", "Smith", newDateStr, newDateStr);

            mapper.updateEntity(request, existing);

            assertThat(existing)
                    .returns("Jane",               from(Employee::getFirstName))
                    .returns("B",                  from(Employee::getMiddleInitial))
                    .returns("Smith",              from(Employee::getLastName))
                    .returns(EmployeeStatus.ACTIVE, from(Employee::getStatus))
                    .returns(newDate,              from(Employee::getDateOfBirth))
                    .returns(newDate,              from(Employee::getDateOfEmployment));
        }

        @Test
        @DisplayName("preserves the original entity id after update")
        void preservesEntityId() {
            Employee existing = buildEmployee(EMPLOYEE_ID, DATE_LOCAL, DATE_LOCAL, EmployeeStatus.ACTIVE);
            EmployeeRequest request = buildRequest(DATE_STRING, DATE_STRING);

            mapper.updateEntity(request, existing);

            assertThat(existing.getId()).isEqualTo(EMPLOYEE_ID);
        }

        @Test
        @DisplayName("sets status back to ACTIVE even if entity was inactive")
        void resetsStatusToActive() {
            Employee existing = buildEmployee(EMPLOYEE_ID, DATE_LOCAL, DATE_LOCAL, EmployeeStatus.INACTIVE);
            EmployeeRequest request = buildRequest(DATE_STRING, DATE_STRING);

            mapper.updateEntity(request, existing);

            assertThat(existing.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        }
    }

    // =========================================================================
    // FORMATTER constant
    // =========================================================================
    @Nested
    @DisplayName("FORMATTER constant")
    class FormatterTests {

        @Test
        @DisplayName("formats a LocalDate as dd-MM-yyyy")
        void formatsDateCorrectly() {
            String formatted = EmployeeMapperImpl.FORMATTER.format(DATE_LOCAL);

            assertThat(formatted).isEqualTo(DATE_STRING);
        }

        @Test
        @DisplayName("parses a dd-MM-yyyy string back to the correct LocalDate")
        void parsesDateCorrectly() {
            LocalDate parsed = LocalDate.parse(DATE_STRING, EmployeeMapperImpl.FORMATTER);

            assertThat(parsed).isEqualTo(DATE_LOCAL);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Employee buildEmployee(String id,
                                   LocalDate dateOfBirth,
                                   LocalDate dateOfEmployment,
                                   EmployeeStatus status) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFirstName(FIRST_NAME);
        employee.setMiddleInitial(MIDDLE_INITIAL);
        employee.setLastName(LAST_NAME);
        employee.setDateOfBirth(dateOfBirth);
        employee.setDateOfEmployment(dateOfEmployment);
        employee.setStatus(status);
        return employee;
    }

    private EmployeeRequest buildRequest(String dateOfBirth, String dateOfEmployment) {
        return new EmployeeRequest(FIRST_NAME, MIDDLE_INITIAL, LAST_NAME, dateOfBirth, dateOfEmployment);
    }
}
