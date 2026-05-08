package com.authserver.integration;

import com.authserver.TestSecurityConfig;
import com.authserver.dto.UserCreateRequest;
import com.authserver.dto.UserResponse;
import com.authserver.dto.UserUpdateRequest;
import com.authserver.controller.UserManagementController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.authserver.service.UserManagementService;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserManagementController.class)
@Import(TestSecurityConfig.class)
class UserCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserManagementService userManagementService;

    @Test
    void testCompleteUserCrudFlow() throws Exception {
        // Step 1: Create a new user
        UserCreateRequest createRequest = new UserCreateRequest();
        createRequest.setUsername("integrationtest");
        createRequest.setFirstName("Integration");
        createRequest.setLastName("Test");
        createRequest.setEmail("integration@test.com");
        createRequest.setPassword("password123");
        createRequest.setRealmRoles(Arrays.asList("user"));

        UserResponse createdUser = new UserResponse();
        createdUser.setId("123");
        createdUser.setUsername("integrationtest");
        createdUser.setFirstName("Integration");
        createdUser.setLastName("Test");
        createdUser.setEmail("integration@test.com");
        createdUser.setEnabled(true);
        createdUser.setEmailVerified(false);

        when(userManagementService.createUser(any(UserCreateRequest.class))).thenReturn(createdUser);

        String createResponse = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("integrationtest"))
                .andExpect(jsonPath("$.firstName").value("Integration"))
                .andExpect(jsonPath("$.lastName").value("Test"))
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andReturn().getResponse().getContentAsString();

        String userId = "123";

        // Step 2: Get user by ID
        when(userManagementService.getUserById(userId)).thenReturn(createdUser);
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("integrationtest"));

        // Step 3: Get user by username
        when(userManagementService.getUserByUsername("integrationtest")).thenReturn(createdUser);
        mockMvc.perform(get("/api/users/username/{username}", "integrationtest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("integrationtest"))
                .andExpect(jsonPath("$.firstName").value("Integration"));

        // Step 4: Update the user
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setEmail("updated@test.com");
        updateRequest.setRealmRolesToAdd(Arrays.asList("admin"));
        updateRequest.setRealmRolesToRemove(Arrays.asList("user"));

        UserResponse updatedUser = new UserResponse();
        updatedUser.setId(userId);
        updatedUser.setUsername("integrationtest");
        updatedUser.setFirstName("Updated");
        updatedUser.setEmail("updated@test.com");

        when(userManagementService.updateUser(eq(userId), any(UserUpdateRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.email").value("updated@test.com"));

        // Step 5: Get all users and verify our user is in the list
        List<UserResponse> users = Arrays.asList(createdUser);
        when(userManagementService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("integrationtest"));

        // Step 6: Delete the user
        doNothing().when(userManagementService).deleteUser(userId);
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        // Step 7: Verify user is deleted
        when(userManagementService.getUserById(userId)).thenThrow(new RuntimeException("User not found"));
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testCreateUserWithAttributes() throws Exception {
        // Given
        UserCreateRequest createRequest = new UserCreateRequest();
        createRequest.setUsername("attributetest");
        createRequest.setFirstName("Attribute");
        createRequest.setLastName("Test");
        createRequest.setEmail("attribute@test.com");
        createRequest.setPassword("password123");

        Map<String, java.util.List<String>> attributes = new HashMap<>();
        attributes.put("department", Arrays.asList("IT"));
        attributes.put("location", Arrays.asList("Remote"));
        createRequest.setAttributes(attributes);

        UserResponse createdUser = new UserResponse();
        createdUser.setId("123");
        createdUser.setUsername("attributetest");
        createdUser.setFirstName("Attribute");
        createdUser.setLastName("Test");
        createdUser.setEmail("attribute@test.com");
        createdUser.setEnabled(true);
        createdUser.setEmailVerified(false);

        when(userManagementService.createUser(any(UserCreateRequest.class))).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("attributetest"))
                .andExpect(jsonPath("$.firstName").value("Attribute"))
                .andExpect(jsonPath("$.lastName").value("Test"))
                .andExpect(jsonPath("$.email").value("attribute@test.com"));
    }

    @Test
    void testCreateUserWithInvalidData() throws Exception {
        // Given
        UserCreateRequest invalidRequest = new UserCreateRequest();
        invalidRequest.setUsername(""); // Invalid
        invalidRequest.setEmail("invalid-email"); // Invalid
        invalidRequest.setPassword("123"); // Too short

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUserWithInvalidData() throws Exception {
        // Given
        UserUpdateRequest invalidRequest = new UserUpdateRequest();
        invalidRequest.setEmail("invalid-email"); // Invalid

        // When & Then
        mockMvc.perform(put("/api/users/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetNonExistentUser() throws Exception {
        // Given
        when(userManagementService.getUserById("nonexistent")).thenThrow(new RuntimeException("User not found"));
        when(userManagementService.getUserByUsername("nonexistent")).thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/nonexistent"))
                .andExpect(status().isInternalServerError());

        mockMvc.perform(get("/api/users/username/nonexistent"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testDeleteNonExistentUser() throws Exception {
        // Given
        doThrow(new RuntimeException("User not found")).when(userManagementService).deleteUser("nonexistent");

        // When & Then
        mockMvc.perform(delete("/api/users/nonexistent"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetAllUsersEmpty() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testCreateUserWithDuplicateUsername() throws Exception {
        // Given
        UserCreateRequest request1 = new UserCreateRequest();
        request1.setUsername("duplicate");
        request1.setFirstName("First");
        request1.setLastName("User");
        request1.setEmail("first@test.com");
        request1.setPassword("password123");

        UserResponse createdUser = new UserResponse();
        createdUser.setId("123");
        createdUser.setUsername("duplicate");
        createdUser.setFirstName("First");
        createdUser.setLastName("User");
        createdUser.setEmail("first@test.com");
        createdUser.setEnabled(true);
        createdUser.setEmailVerified(false);

        // Mock first user creation success
        when(userManagementService.createUser(any(UserCreateRequest.class))).thenReturn(createdUser);

        // Create first user
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Try to create user with same username
        UserCreateRequest request2 = new UserCreateRequest();
        request2.setUsername("duplicate");
        request2.setFirstName("Second");
        request2.setLastName("User");
        request2.setEmail("second@test.com");
        request2.setPassword("password123");

        // Mock duplicate username failure
        when(userManagementService.createUser(eq(request2))).thenThrow(new RuntimeException("Username already exists"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isInternalServerError());
    }
}
