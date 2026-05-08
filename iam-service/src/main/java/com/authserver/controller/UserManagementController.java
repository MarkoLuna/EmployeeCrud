package com.authserver.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.authserver.dto.UserCreateRequest;
import com.authserver.dto.UserResponse;
import com.authserver.dto.UserUpdateRequest;
import com.authserver.service.UserManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Endpoints for managing users in Keycloak")
public class UserManagementController {
    
    private final UserManagementService userManagementService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('admin', 'manage-users')")
    @Operation(summary = "Create a new user", description = "Creates a new user in Keycloak with the provided details and roles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully", 
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Creating user: {}", request.getUsername());
        return userManagementService.createUser(request);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'manage-users', 'view-users', 'query-users')")
    @Operation(summary = "Get user by ID", description = "Retrieves user details from Keycloak using the unique user ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found", 
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public UserResponse getUserById(
            @Parameter(description = "ID of the user to retrieve", example = "550e8400-e29b-41d4-a716-446655440000") 
            @PathVariable String id) {
        log.info("Getting user by ID: {}", id);
        return userManagementService.getUserById(id);
    }
    
    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('admin', 'manage-users', 'view-users', 'query-users')")
    @Operation(summary = "Get user by username", description = "Retrieves user details from Keycloak using the username")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found", 
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public UserResponse getUserByUsername(
            @Parameter(description = "Username of the user to retrieve", example = "jdoe")
            @PathVariable String username) {
        log.info("Getting user by username: {}", username);
        return userManagementService.getUserByUsername(username);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'manage-users', 'view-users', 'query-users')")
    @Operation(summary = "Get all users", description = "Retrieves a list of all users from Keycloak")
    @ApiResponse(responseCode = "200", description = "List of users retrieved", 
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    public List<UserResponse> getAllUsers() {
        log.info("Getting all users");
        return userManagementService.getAllUsers();
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'manage-users')")
    @Operation(summary = "Update user", description = "Updates user details and role assignments in Keycloak")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully", 
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public UserResponse updateUser(
            @Parameter(description = "ID of the user to update", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id, 
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user: {}", id);
        return userManagementService.updateUser(id, request);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('admin', 'manage-users')")
    @Operation(summary = "Delete user", description = "Removes a user from Keycloak")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void deleteUser(
            @Parameter(description = "ID of the user to delete", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        log.info("Deleting user: {}", id);
        userManagementService.deleteUser(id);
    }
}
