package com.authserver.service;

import com.authserver.dto.UserCreateRequest;
import com.authserver.dto.UserResponse;
import com.authserver.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import com.authserver.exception.UserManagementException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {
    
    private final RealmResource realmResource;
    
    public UserResponse createUser(UserCreateRequest request) {
        UsersResource usersResource = realmResource.users();
        
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setEnabled(request.isEnabled());
        user.setEmailVerified(request.isEmailVerified());
        
        if (request.getAttributes() != null) {
            user.setAttributes(request.getAttributes());
        }
        
        Response response = usersResource.create(user);
        
        if (response.getStatus() != 201) {
            log.error("Failed to create user: {}", response.getStatus());
            throw new UserManagementException("Failed to create user: " + response.getStatusInfo().getReasonPhrase());
        }
        
        String userId = response.getLocation().getPath().replaceAll(".*/", "");
        log.info("Created user with ID: {}", userId);
        
        // Set password
        UserResource userResource = usersResource.get(userId);
        CredentialRepresentation password = new CredentialRepresentation();
        password.setTemporary(false);
        password.setType(CredentialRepresentation.PASSWORD);
        password.setValue(request.getPassword());
        userResource.resetPassword(password);
        
        // Assign roles
        if (request.getRealmRoles() != null && !request.getRealmRoles().isEmpty()) {
            assignRealmRoles(userId, request.getRealmRoles());
        }
        
        return getUserById(userId);
    }
    
    public UserResponse getUserById(String userId) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();
            return UserResponse.fromKeycloakUser(user);
        } catch (NotFoundException e) {
            log.error("User not found with ID: {}", userId);
            throw new UserManagementException("User not found with ID: " + userId, HttpStatus.NOT_FOUND);
        }
    }
    
    public List<UserResponse> getAllUsers() {
        List<UserRepresentation> users = realmResource.users().list();
        return users.stream()
                .map(UserResponse::fromKeycloakUser)
                .collect(Collectors.toList());
    }
    
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();
            
            if (request.getFirstName() != null) {
                user.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null) {
                user.setLastName(request.getLastName());
            }
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }
            if (request.getEnabled() != null) {
                user.setEnabled(request.getEnabled());
            }
            if (request.getEmailVerified() != null) {
                user.setEmailVerified(request.getEmailVerified());
            }
            if (request.getAttributes() != null) {
                user.setAttributes(request.getAttributes());
            }
            
            userResource.update(user);
            
            // Handle role assignments
            if (request.getRealmRolesToAdd() != null && !request.getRealmRolesToAdd().isEmpty()) {
                assignRealmRoles(userId, request.getRealmRolesToAdd());
            }
            
            if (request.getRealmRolesToRemove() != null && !request.getRealmRolesToRemove().isEmpty()) {
                removeRealmRoles(userId, request.getRealmRolesToRemove());
            }
            
            return getUserById(userId);
        } catch (NotFoundException e) {
            log.error("Cannot update user, not found with ID: {}", userId);
            throw new UserManagementException("User not found with ID: " + userId, HttpStatus.NOT_FOUND);
        }
    }
    
    public void deleteUser(String userId) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            userResource.remove();
            log.info("Deleted user with ID: {}", userId);
        } catch (NotFoundException e) {
            log.error("Cannot delete user, not found with ID: {}", userId);
            throw new UserManagementException("User not found with ID: " + userId, HttpStatus.NOT_FOUND);
        }
    }
    
    private void assignRealmRoles(String userId, List<String> roleNames) {
        UserResource userResource = realmResource.users().get(userId);
        List<org.keycloak.representations.idm.RoleRepresentation> roles = roleNames.stream()
                .map(roleName -> realmResource.roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());
        userResource.roles().realmLevel().add(roles);
    }
    
    private void removeRealmRoles(String userId, List<String> roleNames) {
        UserResource userResource = realmResource.users().get(userId);
        List<org.keycloak.representations.idm.RoleRepresentation> roles = roleNames.stream()
                .map(roleName -> realmResource.roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());
        userResource.roles().realmLevel().remove(roles);
    }
    
    public UserResponse getUserByUsername(String username) {
        List<UserRepresentation> users = realmResource.users().search(username, 0, 1);
        if (users.isEmpty()) {
            log.error("User not found with username: {}", username);
            throw new UserManagementException("User not found with username: " + username, HttpStatus.NOT_FOUND);
        }
        return UserResponse.fromKeycloakUser(users.get(0));
    }
}
