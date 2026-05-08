package com.employee.services;

import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.entities.Employee;
import com.employee.enums.EmployeeStatus;
import com.employee.exceptions.EmployeeNotFound;
import com.employee.mappers.EmployeeMapper;
import com.employee.repositories.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService")
class EmployeeServiceTest {

    private static final String DATE_STRING = "17-09-2012";
    private static final LocalDate DATE_LOCAL = LocalDate.of(2012, 9, 17);
    private static final String FIRST_NAME = "John";
    private static final String MIDDLE_INITIAL = "A";
    private static final String LAST_NAME = "Doe";
    private static final String EMPLOYEE_ID = "abc-123";

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    // =========================================================================
    // list()
    // =========================================================================
    @Nested
    @DisplayName("list()")
    class ListTests {

        @Test
        @DisplayName("returns a mapped page of EmployeeDto")
        void returnsMappedPage() {
            Employee employee = buildEmployee();
            EmployeeDto dto = buildDto();
            Page<Employee> entityPage = new PageImpl<>(List.of(employee));

            Sort sort = Sort.by(Sort.Direction.DESC, "dateOfEmployment");
            when(employeeRepository.findByStatus(EmployeeStatus.ACTIVE, PageRequest.of(0, 10, sort)))
                    .thenReturn(entityPage);
            when(employeeMapper.toDto(employee)).thenReturn(dto);

            Page<EmployeeDto> result = employeeService.list(0, 10);

            assertThat(result.getContent()).containsExactly(dto);
        }

        @Test
        @DisplayName("queries with ACTIVE status and DESC dateOfEmployment sort")
        void usesCorrectStatusAndSort() {
            Sort sort = Sort.by(Sort.Direction.DESC, "dateOfEmployment");
            when(employeeRepository.findByStatus(eq(EmployeeStatus.ACTIVE), any()))
                    .thenReturn(Page.empty());

            employeeService.list(0, 5);

            verify(employeeRepository)
                    .findByStatus(EmployeeStatus.ACTIVE, PageRequest.of(0, 5, sort));
        }

        @Test
        @DisplayName("returns an empty page when no active employees exist")
        void returnsEmptyPage() {
            when(employeeRepository.findByStatus(eq(EmployeeStatus.ACTIVE), any()))
                    .thenReturn(Page.empty());

            Page<EmployeeDto> result = employeeService.list(0, 10);

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // getEmployee()
    // =========================================================================
    @Nested
    @DisplayName("getEmployee()")
    class GetEmployeeTests {

        @Test
        @DisplayName("returns mapped EmployeeDto when employee is found")
        void returnsDtoWhenFound() {
            Employee employee = buildEmployee();
            EmployeeDto dto = buildDto();

            when(employeeRepository.findByIdAndStatus(EMPLOYEE_ID, EmployeeStatus.ACTIVE))
                    .thenReturn(Optional.of(employee));
            when(employeeMapper.toDto(employee)).thenReturn(dto);

            EmployeeDto result = employeeService.getEmployee(EMPLOYEE_ID);

            assertThat(result)
                    .returns(EMPLOYEE_ID, from(EmployeeDto::getId))
                    .returns(FIRST_NAME, from(EmployeeDto::getFirstName))
                    .returns(LAST_NAME, from(EmployeeDto::getLastName));
        }

        @Test
        @DisplayName("throws EmployeeNotFound when employee does not exist")
        void throwsWhenNotFound() {
            when(employeeRepository.findByIdAndStatus(EMPLOYEE_ID, EmployeeStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployee(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFound.class)
                    .hasMessage("Unable to find the Employee");
        }
    }

    // =========================================================================
    // createEmployee()
    // =========================================================================
    @Nested
    @DisplayName("createEmployee()")
    class CreateEmployeeTests {

        @Test
        @DisplayName("saves the entity and returns the mapped DTO when no duplicate exists")
        void createsAndReturnsDtoWhenNoDuplicate() {
            EmployeeRequest request = buildRequest();
            Employee entity = buildEmployee();
            EmployeeDto dto = buildDto();

            when(employeeRepository.findByFirstNameAndMiddleInitialAndLastNameAndStatus(
                    FIRST_NAME, MIDDLE_INITIAL, LAST_NAME, EmployeeStatus.ACTIVE))
                    .thenReturn(List.of());
            when(employeeMapper.toEntity(request)).thenReturn(entity);
            when(employeeMapper.toDto(entity)).thenReturn(dto);

            Optional<EmployeeDto> result = employeeService.createEmployee(request);

            assertThat(result).isPresent().contains(dto);
            verify(employeeRepository).save(entity);
        }

        @Test
        @DisplayName("returns empty Optional when a duplicate active employee already exists")
        void returnsEmptyWhenDuplicateExists() {
            EmployeeRequest request = buildRequest();

            when(employeeRepository.findByFirstNameAndMiddleInitialAndLastNameAndStatus(
                    FIRST_NAME, MIDDLE_INITIAL, LAST_NAME, EmployeeStatus.ACTIVE))
                    .thenReturn(List.of(buildEmployee()));

            Optional<EmployeeDto> result = employeeService.createEmployee(request);

            assertThat(result).isEmpty();
            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("delegates entity construction to the mapper")
        void delegatesEntityCreationToMapper() {
            EmployeeRequest request = buildRequest();
            Employee entity = buildEmployee();

            when(employeeRepository.findByFirstNameAndMiddleInitialAndLastNameAndStatus(
                    any(), any(), any(), any()))
                    .thenReturn(List.of());
            when(employeeMapper.toEntity(request)).thenReturn(entity);
            when(employeeMapper.toDto(entity)).thenReturn(buildDto());

            employeeService.createEmployee(request);

            verify(employeeMapper).toEntity(request);
        }
    }

    // =========================================================================
    // updateEmployee()
    // =========================================================================
    @Nested
    @DisplayName("updateEmployee()")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("updates entity via mapper, saves it, and returns mapped DTO")
        void updatesAndReturnsDto() {
            EmployeeRequest request = buildRequest();
            Employee employee = buildEmployee();
            EmployeeDto dto = buildDto();

            when(employeeRepository.findByIdAndStatus(EMPLOYEE_ID, EmployeeStatus.ACTIVE))
                    .thenReturn(Optional.of(employee));
            when(employeeMapper.toDto(employee)).thenReturn(dto);

            EmployeeDto result = employeeService.updateEmployee(EMPLOYEE_ID, request);

            assertThat(result)
                    .returns(EMPLOYEE_ID, from(EmployeeDto::getId))
                    .returns(FIRST_NAME, from(EmployeeDto::getFirstName));

            verify(employeeMapper).updateEntity(request, employee);
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("throws EmployeeNotFound when employee does not exist")
        void throwsWhenNotFound() {
            when(employeeRepository.findByIdAndStatus(EMPLOYEE_ID, EmployeeStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.updateEmployee(EMPLOYEE_ID, buildRequest()))
                    .isInstanceOf(EmployeeNotFound.class)
                    .hasMessage("Unable to find the employee");
        }

        @Test
        @DisplayName("passes the correct employee to the mapper for update")
        void passesCorrectEntityToMapper() {
            Employee employee = buildEmployee();
            EmployeeRequest request = buildRequest();

            when(employeeRepository.findByIdAndStatus(EMPLOYEE_ID, EmployeeStatus.ACTIVE))
                    .thenReturn(Optional.of(employee));
            when(employeeMapper.toDto(employee)).thenReturn(buildDto());

            employeeService.updateEmployee(EMPLOYEE_ID, request);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeMapper).updateEntity(eq(request), captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(EMPLOYEE_ID);
        }
    }

    // =========================================================================
    // deleteEmployee()
    // =========================================================================
    @Nested
    @DisplayName("deleteEmployee()")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("deletes the employee when found")
        void deletesWhenFound() {
            Employee employee = buildEmployee();
            when(employeeRepository.findByIdAndStatus(EMPLOYEE_ID, EmployeeStatus.ACTIVE))
                    .thenReturn(Optional.of(employee));

            employeeService.deleteEmployee(EMPLOYEE_ID);

            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("throws EmployeeNotFound when employee does not exist")
        void throwsWhenNotFound() {
            when(employeeRepository.findByIdAndStatus(EMPLOYEE_ID, EmployeeStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteEmployee(EMPLOYEE_ID))
                    .isInstanceOf(EmployeeNotFound.class)
                    .hasMessage("Unable to find the employee");
        }

        @Test
        @DisplayName("does not call delete when employee is not found")
        void doesNotDeleteWhenNotFound() {
            when(employeeRepository.findByIdAndStatus(EMPLOYEE_ID, EmployeeStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            try {
                employeeService.deleteEmployee(EMPLOYEE_ID);
            } catch (EmployeeNotFound ignored) {
            }

            verify(employeeRepository, never()).delete(any());
        }
    }

    // =========================================================================
    // hasValidDates()
    // =========================================================================
    @Nested
    @DisplayName("hasValidDates()")
    class HasValidDatesTests {

        @Test
        @DisplayName("returns empty Optional when both dates are valid")
        void returnsEmptyWhenBothDatesValid() {
            EmployeeRequest request = buildRequest();

            Optional<String> result = employeeService.hasValidDates(request);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns error message when dateOfBirth is invalid")
        void returnsErrorForInvalidDateOfBirth() {
            EmployeeRequest request = new EmployeeRequest(FIRST_NAME, MIDDLE_INITIAL, LAST_NAME,
                    "not-a-date", DATE_STRING);

            Optional<String> result = employeeService.hasValidDates(request);

            assertThat(result).isPresent().contains("Invalid Date Of Birth");
        }

        @Test
        @DisplayName("returns error message when dateOfBirth is null")
        void returnsErrorForNullDateOfBirth() {
            EmployeeRequest request = new EmployeeRequest(FIRST_NAME, MIDDLE_INITIAL, LAST_NAME,
                    null, DATE_STRING);

            Optional<String> result = employeeService.hasValidDates(request);

            assertThat(result).isPresent().contains("Invalid Date Of Birth");
        }

        @Test
        @DisplayName("returns error message when dateOfBirth is empty")
        void returnsErrorForEmptyDateOfBirth() {
            EmployeeRequest request = new EmployeeRequest(FIRST_NAME, MIDDLE_INITIAL, LAST_NAME,
                    "", DATE_STRING);

            Optional<String> result = employeeService.hasValidDates(request);

            assertThat(result).isPresent().contains("Invalid Date Of Birth");
        }

        @Test
        @DisplayName("returns error message when dateOfEmployment is invalid")
        void returnsErrorForInvalidDateOfEmployment() {
            EmployeeRequest request = new EmployeeRequest(FIRST_NAME, MIDDLE_INITIAL, LAST_NAME,
                    DATE_STRING, "bad-date");

            Optional<String> result = employeeService.hasValidDates(request);

            assertThat(result).isPresent().contains("Invalid Date Of Employment");
        }

        @Test
        @DisplayName("returns error message when dateOfEmployment is null")
        void returnsErrorForNullDateOfEmployment() {
            EmployeeRequest request = new EmployeeRequest(FIRST_NAME, MIDDLE_INITIAL, LAST_NAME,
                    DATE_STRING, null);

            Optional<String> result = employeeService.hasValidDates(request);

            assertThat(result).isPresent().contains("Invalid Date Of Employment");
        }

        @Test
        @DisplayName("returns error message when dateOfEmployment is empty")
        void returnsErrorForEmptyDateOfEmployment() {
            EmployeeRequest request = new EmployeeRequest(FIRST_NAME, MIDDLE_INITIAL, LAST_NAME,
                    DATE_STRING, "");

            Optional<String> result = employeeService.hasValidDates(request);

            assertThat(result).isPresent().contains("Invalid Date Of Employment");
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Employee buildEmployee() {
        Employee employee = new Employee();
        employee.setId(EMPLOYEE_ID);
        employee.setFirstName(FIRST_NAME);
        employee.setMiddleInitial(MIDDLE_INITIAL);
        employee.setLastName(LAST_NAME);
        employee.setDateOfBirth(DATE_LOCAL);
        employee.setDateOfEmployment(DATE_LOCAL);
        employee.setStatus(EmployeeStatus.ACTIVE);
        return employee;
    }

    private EmployeeDto buildDto() {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(EMPLOYEE_ID);
        dto.setFirstName(FIRST_NAME);
        dto.setMiddleInitial(MIDDLE_INITIAL);
        dto.setLastName(LAST_NAME);
        dto.setDateOfBirth(DATE_STRING);
        dto.setDateOfEmployment(DATE_STRING);
        dto.setStatus(EmployeeStatus.ACTIVE.name());
        return dto;
    }

    private EmployeeRequest buildRequest() {
        return new EmployeeRequest(FIRST_NAME, MIDDLE_INITIAL, LAST_NAME, DATE_STRING, DATE_STRING);
    }
}
