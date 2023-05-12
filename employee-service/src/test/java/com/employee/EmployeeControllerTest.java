package com.employee;

import java.time.LocalDate;

import com.employee.dto.EmployeeRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        EmployeeCrudApplication.class,
})
@DisplayName("Employee tests")
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    private static final LocalDate BASIC_DATE = LocalDate.of(2012, 9, 17);

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("List all employees")
    @WithMockUser
    @Test
    public void getAllEmployees() throws Exception {

        mockMvc.perform(get("/employees/{page}/{total}", 0, 10)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id").isNotEmpty());
    }

    @DisplayName("Attempt get all employees and has unauthorized status")
    @Test
    public void getAllEmployeesUnAuthorized() throws Exception {
        mockMvc.perform(get("/employees/{page}/{total}", 0, 10)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Get employee by Id")
    @WithMockUser
    @Test
    public void getEmployeeById() throws Exception {
        mockMvc.perform(get("/employees/{id}", "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4")
                .accept(MediaType.APPLICATION_JSON))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4"));
    }

    @DisplayName("Create a new employee")
    @WithMockUser
    @Test
    public void createEmployee() throws Exception {
        mockMvc.perform(post("/employees")
                .content(asJsonString(new EmployeeRequest("Gerardo2", "J", "Luna", BASIC_DATE, BASIC_DATE)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @DisplayName("Create a new employee with invalid request")
    @WithMockUser
    @Test
    public void createEmployeeWithInvalidRequest() throws Exception {
        mockMvc.perform(post("/employees")
                        .content(asJsonString(new EmployeeRequest("", "", "", null, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Update employee")
    @WithMockUser
    @Test
    public void updateEmployee() throws Exception {
        mockMvc.perform(put("/employees/{id}", "e26b1ed4-a8d0-11e9-a2a3-2a2ae2dbcce4")
                .content(asJsonString(new EmployeeRequest("Gerardo", "J", "Luna", BASIC_DATE, BASIC_DATE)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Gerardo"))
                .andExpect(jsonPath("$.lastName").value("Luna"));
    }

    @DisplayName("Delete employee")
    @WithMockUser
    @Test
    public void deleteEmployee() throws Exception {
        mockMvc.perform(delete("/employees/{id}", "e26b1d76-a8d0-11e9-a2a3-2a2ae2dbcce4")
                )
                .andExpect(status().isOk());
    }

    public static String asJsonString(final Object obj) {
        try {
            var objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.registerModule(new Jdk8Module());
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
