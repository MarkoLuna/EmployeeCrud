package com.authserver.controller;

import com.authserver.TestSecurityConfig;
import com.authserver.dto.UserCreateRequest;
import com.authserver.dto.UserResponse;
import com.authserver.dto.UserUpdateRequest;
import com.authserver.exception.UserManagementException;
import com.authserver.service.UserManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserManagementController.class)
@Import(TestSecurityConfig.class)
class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagementService userManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse sampleUser;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        sampleUser = new UserResponse();
        sampleUser.setId("123");
        sampleUser.setUsername("testuser");
        sampleUser.setFirstName("Test");
        sampleUser.setLastName("User");
        sampleUser.setEmail("test@example.com");
        sampleUser.setEnabled(true);
        sampleUser.setEmailVerified(false);

        createRequest = new UserCreateRequest();
        createRequest.setUsername("testuser");
        createRequest.setFirstName("Test");
        createRequest.setLastName("User");
        createRequest.setEmail("test@example.com");
        createRequest.setPassword("password123");

        updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setEmail("updated@example.com");
    }

    @Test
    @WithMockUser
    void testCreateUser_Success() throws Exception {
        when(userManagementService.createUser(any(UserCreateRequest.class))).thenReturn(sampleUser);

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.emailVerified").value(false));

        verify(userManagementService).createUser(any(UserCreateRequest.class));
    }

    @Test
    @WithMockUser
    void testCreateUser_ValidationError() throws Exception {
        UserCreateRequest invalidRequest = new UserCreateRequest();
        invalidRequest.setUsername("");
        invalidRequest.setEmail("invalid-email");
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userManagementService, never()).createUser(any(UserCreateRequest.class));
    }

    @Test
    @WithMockUser
    void testGetUserById_Success() throws Exception {
        String userId = "123";
        when(userManagementService.getUserById(userId)).thenReturn(sampleUser);
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userManagementService).getUserById(userId);
    }

    @Test
    @WithMockUser
    void testGetUserById_NotFound() throws Exception {
        String userId = "nonexistent";
        when(userManagementService.getUserById(userId))
                .thenThrow(new UserManagementException("User not found", HttpStatus.NOT_FOUND));
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userManagementService).getUserById(userId);
    }

    @Test
    @WithMockUser
    void testGetUserByUsername_Success() throws Exception {
        String username = "testuser";
        when(userManagementService.getUserByUsername(username)).thenReturn(sampleUser);
        mockMvc.perform(get("/api/users/username/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userManagementService).getUserByUsername(username);
    }

    @Test
    @WithMockUser
    void testGetUserByUsername_NotFound() throws Exception {
        String username = "nonexistent";
        when(userManagementService.getUserByUsername(username))
                .thenThrow(new UserManagementException("User not found", HttpStatus.NOT_FOUND));
        mockMvc.perform(get("/api/users/username/{username}", username))
                .andExpect(status().isNotFound());

        verify(userManagementService).getUserByUsername(username);
    }

    @Test
    @WithMockUser
    void testGetAllUsers_Success() throws Exception {
        UserResponse user2 = new UserResponse();
        user2.setId("456");
        user2.setUsername("user2");
        user2.setFirstName("User");
        user2.setLastName("Two");

        List<UserResponse> users = Arrays.asList(sampleUser, user2);
        when(userManagementService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("123"))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[1].id").value("456"))
                .andExpect(jsonPath("$[1].username").value("user2"));

        verify(userManagementService).getAllUsers();
    }

    @Test
    @WithMockUser
    void testUpdateUser_Success() throws Exception {
        String userId = "123";
        UserResponse updatedUser = new UserResponse();
        updatedUser.setId(userId);
        updatedUser.setUsername("testuser");
        updatedUser.setFirstName("Updated");
        updatedUser.setEmail("updated@example.com");

        when(userManagementService.updateUser(eq(userId), any(UserUpdateRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userManagementService).updateUser(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    @WithMockUser
    void testUpdateUser_NotFound() throws Exception {
        String userId = "nonexistent";
        when(userManagementService.updateUser(eq(userId), any(UserUpdateRequest.class)))
                .thenThrow(new UserManagementException("User not found", HttpStatus.NOT_FOUND));
        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(userManagementService).updateUser(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    @WithMockUser
    void testUpdateUser_ValidationError() throws Exception {
        String userId = "123";
        UserUpdateRequest invalidRequest = new UserUpdateRequest();
        invalidRequest.setEmail("invalid-email"); // Invalid: not a valid email
        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userManagementService, never()).updateUser(anyString(), any(UserUpdateRequest.class));
    }

    @Test
    @WithMockUser
    void testDeleteUser_Success() throws Exception {
        String userId = "123";
        doNothing().when(userManagementService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{id}", userId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userManagementService).deleteUser(userId);
    }

    @Test
    @WithMockUser
    void testDeleteUser_NotFound() throws Exception {
        String userId = "nonexistent";
        doThrow(new UserManagementException("User not found", HttpStatus.NOT_FOUND))
                .when(userManagementService).deleteUser(userId);
        mockMvc.perform(delete("/api/users/{id}", userId)
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userManagementService).deleteUser(userId);
    }

    @Test
    @WithMockUser
    void testCreateUserWithRoles_Success() throws Exception {
        createRequest.setRealmRoles(Arrays.asList("user", "admin"));
        when(userManagementService.createUser(any(UserCreateRequest.class))).thenReturn(sampleUser);
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userManagementService).createUser(any(UserCreateRequest.class));
    }

    @Test
    @WithMockUser
    void testCreateUserWithAttributes_Success() throws Exception {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("department", Arrays.asList("IT"));
        attributes.put("location", Arrays.asList("New York"));
        createRequest.setAttributes(attributes);

        when(userManagementService.createUser(any(UserCreateRequest.class))).thenReturn(sampleUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("123"));

        verify(userManagementService).createUser(any(UserCreateRequest.class));
    }

    @Test
    @WithMockUser
    void testUpdateUserWithRoleChanges_Success() throws Exception {
        String userId = "123";
        updateRequest.setRealmRolesToAdd(Arrays.asList("admin"));
        updateRequest.setRealmRolesToRemove(Arrays.asList("user"));

        UserResponse updatedUser = new UserResponse();
        updatedUser.setId(userId);
        updatedUser.setUsername("testuser");
        updatedUser.setFirstName("Updated");

        when(userManagementService.updateUser(eq(userId), any(UserUpdateRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.firstName").value("Updated"));

        verify(userManagementService).updateUser(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    @WithMockUser
    void testGetAllUsers_EmptyList_Success() throws Exception {
        when(userManagementService.getAllUsers()).thenReturn(Arrays.asList());
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userManagementService).getAllUsers();
    }
}
