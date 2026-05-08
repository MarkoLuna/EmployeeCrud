package com.employee;

import com.employee.controllers.EmployeeController;
import com.employee.dto.EmployeeDto;
import com.employee.dto.EmployeeRequest;
import com.employee.exceptions.EmployeeNotFound;
import com.employee.exceptions.handlers.GeneralExceptionHandler;
import com.employee.services.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import({ SecurityConfiguration.class, GeneralExceptionHandler.class })
@DisplayName("EmployeeController")
public class EmployeeControllerTest {

    private static final String BASE_URL = "/employees";
    private static final String EMPLOYEE_ID = "abc-123";
    private static final String DATE_STRING = "17-09-2012";
    private static final String FIRST_NAME = "John";
    private static final String MIDDLE_INIT = "A";
    private static final String LAST_NAME = "Doe";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    // =========================================================================
    // GET /employees/{page}/{size}
    // =========================================================================
    @Nested
    @DisplayName("GET /employees/{page}/{size}")
    class ListEmployees {

        @Test
        @WithMockUser
        @DisplayName("returns 200 with a page of employees")
        void returns200WithPage() throws Exception {
            Page<EmployeeDto> page = new PageImpl<>(List.of(buildDto()));
            when(employeeService.list(0, 10)).thenReturn(page);

            mockMvc.perform(get(BASE_URL + "/{page}/{size}", 0, 10)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(EMPLOYEE_ID))
                    .andExpect(jsonPath("$.content[0].firstName").value(FIRST_NAME));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 with empty content when no employees exist")
        void returns200WithEmptyContent() throws Exception {
            when(employeeService.list(0, 10)).thenReturn(Page.empty());

            mockMvc.perform(get(BASE_URL + "/{page}/{size}", 0, 10)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("returns 401 when user is not authenticated")
        void returns401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{page}/{size}", 0, 10)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /employees/{id}
    // =========================================================================
    @Nested
    @DisplayName("GET /employees/{id}")
    class GetEmployee {

        @Test
        @WithMockUser
        @DisplayName("returns 200 with the employee DTO when found")
        void returns200WhenFound() throws Exception {
            when(employeeService.getEmployee(EMPLOYEE_ID)).thenReturn(buildDto());

            mockMvc.perform(get(BASE_URL + "/{id}", EMPLOYEE_ID)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(EMPLOYEE_ID))
                    .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                    .andExpect(jsonPath("$.lastName").value(LAST_NAME));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when employee is not found")
        void returns404WhenNotFound() throws Exception {
            when(employeeService.getEmployee(EMPLOYEE_ID))
                    .thenThrow(new EmployeeNotFound("Unable to find the Employee"));

            mockMvc.perform(get(BASE_URL + "/{id}", EMPLOYEE_ID)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 401 when user is not authenticated")
        void returns401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", EMPLOYEE_ID)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // POST /employees
    // =========================================================================
    @Nested
    @DisplayName("POST /employees")
    class CreateEmployee {

        @Test
        @WithMockUser
        @DisplayName("returns 200 with the created employee DTO")
        void returns200WhenCreated() throws Exception {
            EmployeeRequest request = buildRequest();
            when(employeeService.hasValidDates(any())).thenReturn(Optional.empty());
            when(employeeService.createEmployee(any())).thenReturn(Optional.of(buildDto()));

            mockMvc.perform(post(BASE_URL)
                    .content(toJson(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(EMPLOYEE_ID))
                    .andExpect(jsonPath("$.firstName").value(FIRST_NAME));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when date validation fails")
        void returns400WhenDatesInvalid() throws Exception {
            when(employeeService.hasValidDates(any()))
                    .thenReturn(Optional.of("Invalid Date Of Birth"));

            mockMvc.perform(post(BASE_URL)
                    .content(toJson(buildRequest()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when employee already exists")
        void returns400WhenDuplicateEmployee() throws Exception {
            when(employeeService.hasValidDates(any())).thenReturn(Optional.empty());
            when(employeeService.createEmployee(any())).thenReturn(Optional.empty());

            mockMvc.perform(post(BASE_URL)
                    .content(toJson(buildRequest()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when request body fails bean validation")
        void returns400WhenRequestBodyIsInvalid() throws Exception {
            // firstName is blank — violates @NotBlank
            EmployeeRequest invalid = new EmployeeRequest("", MIDDLE_INIT, LAST_NAME, DATE_STRING, DATE_STRING);

            mockMvc.perform(post(BASE_URL)
                    .content(toJson(invalid))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 401 when user is not authenticated")
        void returns401WhenUnauthenticated() throws Exception {
            mockMvc.perform(post(BASE_URL)
                    .content(toJson(buildRequest()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // PUT /employees/{id}
    // =========================================================================
    @Nested
    @DisplayName("PUT /employees/{id}")
    class UpdateEmployee {

        @Test
        @WithMockUser
        @DisplayName("returns 200 with the updated employee DTO")
        void returns200WhenUpdated() throws Exception {
            EmployeeDto updated = buildDto();
            updated.setFirstName("Jane");
            when(employeeService.updateEmployee(eq(EMPLOYEE_ID), any())).thenReturn(updated);

            mockMvc.perform(put(BASE_URL + "/{id}", EMPLOYEE_ID)
                    .content(toJson(buildRequest()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(EMPLOYEE_ID))
                    .andExpect(jsonPath("$.firstName").value("Jane"));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when employee does not exist")
        void returns404WhenNotFound() throws Exception {
            when(employeeService.updateEmployee(eq(EMPLOYEE_ID), any()))
                    .thenThrow(new EmployeeNotFound("Unable to find the employee"));

            mockMvc.perform(put(BASE_URL + "/{id}", EMPLOYEE_ID)
                    .content(toJson(buildRequest()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 401 when user is not authenticated")
        void returns401WhenUnauthenticated() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{id}", EMPLOYEE_ID)
                    .content(toJson(buildRequest()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // DELETE /employees/{id}
    // =========================================================================
    @Nested
    @DisplayName("DELETE /employees/{id}")
    class DeleteEmployee {

        @Test
        @WithMockUser
        @DisplayName("returns 200 when employee is deleted")
        void returns200WhenDeleted() throws Exception {
            doNothing().when(employeeService).deleteEmployee(EMPLOYEE_ID);

            mockMvc.perform(delete(BASE_URL + "/{id}", EMPLOYEE_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when employee does not exist")
        void returns404WhenNotFound() throws Exception {
            doThrow(new EmployeeNotFound("Unable to find the employee"))
                    .when(employeeService).deleteEmployee(EMPLOYEE_ID);

            mockMvc.perform(delete(BASE_URL + "/{id}", EMPLOYEE_ID))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 401 when user is not authenticated")
        void returns401WhenUnauthenticated() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{id}", EMPLOYEE_ID))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private EmployeeDto buildDto() {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(EMPLOYEE_ID);
        dto.setFirstName(FIRST_NAME);
        dto.setMiddleInitial(MIDDLE_INIT);
        dto.setLastName(LAST_NAME);
        dto.setDateOfBirth(DATE_STRING);
        dto.setDateOfEmployment(DATE_STRING);
        dto.setStatus("ACTIVE");
        return dto;
    }

    private EmployeeRequest buildRequest() {
        return new EmployeeRequest(FIRST_NAME, MIDDLE_INIT, LAST_NAME, DATE_STRING, DATE_STRING);
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
