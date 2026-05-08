package com.authserver.service;

import com.authserver.dto.UserCreateRequest;
import com.authserver.dto.UserResponse;
import com.authserver.dto.UserUpdateRequest;
import com.authserver.exception.UserManagementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private Response response;

    @InjectMocks
    private UserManagementService userManagementService;

    @BeforeEach
    void setUp() {
        when(realmResource.users()).thenReturn(usersResource);
    }

    @Test
    void testCreateUser_Success() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setEnabled(true);
        request.setEmailVerified(false);

        java.net.URI mockUri = mock(java.net.URI.class);
        when(mockUri.getPath()).thenReturn("/users/123");
        
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(mockUri);

        UserRepresentation createdUser = new UserRepresentation();
        createdUser.setId("123");
        createdUser.setUsername("testuser");
        createdUser.setFirstName("Test");
        createdUser.setLastName("User");
        createdUser.setEmail("test@example.com");
        createdUser.setEnabled(true);
        createdUser.setEmailVerified(false);

        when(usersResource.get("123")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(createdUser);

        // When
        UserResponse result = userManagementService.createUser(request);

        // Then
        assertThat(result)
            .isNotNull()
            .extracting(
                UserResponse::getUsername,
                UserResponse::getFirstName,
                UserResponse::getLastName,
                UserResponse::getEmail,
                UserResponse::isEnabled,
                UserResponse::isEmailVerified
            )
            .containsExactly(
                "testuser",
                "Test", 
                "User",
                "test@example.com",
                true,
                false
            );

        verify(usersResource).create(any(UserRepresentation.class));
        verify(userResource).resetPassword(any(CredentialRepresentation.class));
    }

    @Test
    void testCreateUser_Failure() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        Response.StatusType statusType = mock(Response.StatusType.class);
        when(statusType.getReasonPhrase()).thenReturn("Bad Request");

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(400);
        when(response.getStatusInfo()).thenReturn(statusType);

        // When & Then
        assertThatThrownBy(() -> userManagementService.createUser(request))
            .isInstanceOf(UserManagementException.class)
            .hasMessageContaining("Failed to create user: Bad Request");
    }

    @Test
    void testGetUserById_Success() {
        // Given
        String userId = "123";
        UserRepresentation user = new UserRepresentation();
        user.setId(userId);
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setEnabled(true);
        user.setEmailVerified(false);

        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(user);

        // When
        UserResponse result = userManagementService.getUserById(userId);

        // Then
        assertThat(result)
            .isNotNull()
            .extracting(
                UserResponse::getId,
                UserResponse::getUsername,
                UserResponse::getFirstName,
                UserResponse::getLastName,
                UserResponse::getEmail
            )
            .containsExactly(
                userId,
                "testuser",
                "Test",
                "User",
                "test@example.com"
            );

        verify(usersResource).get(userId);
        verify(userResource).toRepresentation();
    }

    @Test
    void testGetUserById_NotFound() {
        // Given
        String userId = "nonexistent";
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenThrow(new NotFoundException());

        // When & Then
        assertThatThrownBy(() -> userManagementService.getUserById(userId))
            .isInstanceOf(UserManagementException.class)
            .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
            .hasMessageContaining("User not found with ID: nonexistent");
    }

    @Test
    void testGetAllUsers_Success() {
        // Given
        UserRepresentation user1 = new UserRepresentation();
        user1.setId("1");
        user1.setUsername("user1");
        user1.setFirstName("User");
        user1.setLastName("One");
        user1.setEnabled(true);
        user1.setEmailVerified(false);

        UserRepresentation user2 = new UserRepresentation();
        user2.setId("2");
        user2.setUsername("user2");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setEnabled(true);
        user2.setEmailVerified(false);

        when(usersResource.list()).thenReturn(Arrays.asList(user1, user2));

        // When
        List<UserResponse> result = userManagementService.getAllUsers();

        // Then
        assertThat(result)
            .isNotNull()
            .hasSize(2)
            .extracting(UserResponse::getUsername)
            .containsExactly("user1", "user2");

        verify(usersResource).list();
    }

    @Test
    void testUpdateUser_Success() {
        // Given
        String userId = "123";
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Updated");
        request.setEmail("updated@example.com");

        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setId(userId);
        existingUser.setUsername("testuser");
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");
        existingUser.setEmail("test@example.com");
        existingUser.setEnabled(true);
        existingUser.setEmailVerified(false);

        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(existingUser);

        // When
        UserResponse result = userManagementService.updateUser(userId, request);

        // Then
        assertThat(result)
            .isNotNull()
            .extracting(
                UserResponse::getFirstName,
                UserResponse::getEmail
            )
            .containsExactly(
                "Updated",
                "updated@example.com"
            );

        verify(userResource).update(any(UserRepresentation.class));
    }

    @Test
    void testUpdateUser_NotFound() {
        // Given
        String userId = "nonexistent";
        UserUpdateRequest request = new UserUpdateRequest();
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenThrow(new NotFoundException());

        // When & Then
        assertThatThrownBy(() -> userManagementService.updateUser(userId, request))
            .isInstanceOf(UserManagementException.class)
            .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
            .hasMessageContaining("User not found with ID: nonexistent");
    }

    @Test
    void testDeleteUser_Success() {
        // Given
        String userId = "123";
        when(usersResource.get(userId)).thenReturn(userResource);

        // When
        userManagementService.deleteUser(userId);

        // Then
        verify(userResource).remove();
    }

    @Test
    void testDeleteUser_NotFound() {
        // Given
        String userId = "nonexistent";
        when(usersResource.get(userId)).thenReturn(userResource);
        doThrow(new NotFoundException()).when(userResource).remove();

        // When & Then
        assertThatThrownBy(() -> userManagementService.deleteUser(userId))
            .isInstanceOf(UserManagementException.class)
            .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
            .hasMessageContaining("User not found with ID: nonexistent");
    }

    @Test
    void testGetUserByUsername_Success() {
        // Given
        String username = "testuser";
        UserRepresentation user = new UserRepresentation();
        user.setId("123");
        user.setUsername(username);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEnabled(true);
        user.setEmailVerified(false);

        when(usersResource.search(username, 0, 1)).thenReturn(Arrays.asList(user));

        // When
        UserResponse result = userManagementService.getUserByUsername(username);

        // Then
        assertThat(result)
            .isNotNull()
            .extracting(
                UserResponse::getUsername,
                UserResponse::getFirstName
            )
            .containsExactly(
                username,
                "Test"
            );

        verify(usersResource).search(username, 0, 1);
    }

    @Test
    void testGetUserByUsername_NotFound() {
        // Given
        String username = "nonexistent";
        when(usersResource.search(username, 0, 1)).thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> userManagementService.getUserByUsername(username))
            .isInstanceOf(UserManagementException.class)
            .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
            .hasMessageContaining("User not found with username: nonexistent");
    }

    @Test
    void testCreateUserWithRoles_Success() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRealmRoles(Arrays.asList("user", "admin"));

        java.net.URI mockUri = mock(java.net.URI.class);
        when(mockUri.getPath()).thenReturn("/users/123");
        
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(mockUri);

        UserRepresentation createdUser = new UserRepresentation();
        createdUser.setId("123");
        createdUser.setUsername("testuser");
        createdUser.setEnabled(true);
        createdUser.setEmailVerified(false);

        when(usersResource.get("123")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(createdUser);

        // Mock user roles resource
        org.keycloak.admin.client.resource.RoleMappingResource roleMappingResource = mock(org.keycloak.admin.client.resource.RoleMappingResource.class);
        org.keycloak.admin.client.resource.RoleScopeResource roleScopeResource = mock(org.keycloak.admin.client.resource.RoleScopeResource.class);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        // Mock roles resource
        org.keycloak.admin.client.resource.RolesResource rolesResource = mock(org.keycloak.admin.client.resource.RolesResource.class);
        when(realmResource.roles()).thenReturn(rolesResource);

        RoleRepresentation userRole = new RoleRepresentation();
        userRole.setName("user");

        RoleRepresentation adminRole = new RoleRepresentation();
        adminRole.setName("admin");

        when(rolesResource.get("user")).thenReturn(mock(org.keycloak.admin.client.resource.RoleResource.class));
        when(rolesResource.get("user").toRepresentation()).thenReturn(userRole);
        when(rolesResource.get("admin")).thenReturn(mock(org.keycloak.admin.client.resource.RoleResource.class));
        when(rolesResource.get("admin").toRepresentation()).thenReturn(adminRole);

        // When
        UserResponse result = userManagementService.createUser(request);

        // Then
        assertThat(result).isNotNull();
        verify(usersResource).create(any(UserRepresentation.class));
        verify(userResource).resetPassword(any(CredentialRepresentation.class));
        verify(roleScopeResource).add(anyList());
    }

    @Test
    void testCreateUserWithAttributes_Success() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("department", Arrays.asList("IT"));
        attributes.put("location", Arrays.asList("New York"));
        request.setAttributes(attributes);

        java.net.URI mockUri = mock(java.net.URI.class);
        when(mockUri.getPath()).thenReturn("/users/123");
        
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(mockUri);

        UserRepresentation createdUser = new UserRepresentation();
        createdUser.setId("123");
        createdUser.setUsername("testuser");
        createdUser.setEnabled(true);
        createdUser.setEmailVerified(false);

        when(usersResource.get("123")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(createdUser);

        // When
        UserResponse result = userManagementService.createUser(request);

        // Then
        assertThat(result).isNotNull();
        
        // Capture the UserRepresentation passed to create method
        ArgumentCaptor<UserRepresentation> userCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
        verify(usersResource).create(userCaptor.capture());
        
        UserRepresentation capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getAttributes())
            .isNotNull()
            .containsKey("department");
        assertThat(capturedUser.getAttributes().get("department"))
            .isEqualTo(Arrays.asList("IT"));
    }
}
