package com.authserver.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UserUpdateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidUserUpdateRequest() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Updated");
        request.setEmail("updated@example.com");

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidUserUpdateRequest_AllFields() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Updated");
        request.setLastName("User");
        request.setEmail("updated@example.com");
        request.setEnabled(true);
        request.setEmailVerified(true);
        request.setRealmRolesToAdd(Arrays.asList("admin"));
        request.setRealmRolesToRemove(Arrays.asList("user"));
        request.setClientRolesToAdd(Arrays.asList("client-admin"));
        request.setClientRolesToRemove(Arrays.asList("client-user"));

        Map<String, java.util.List<String>> attributes = new HashMap<>();
        attributes.put("department", Arrays.asList("HR"));
        request.setAttributes(attributes);

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidUserUpdateRequest_EmptyOptionalFields() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        // All fields are null - should be valid as all are optional

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidFirstName_TooLong() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("a".repeat(51));

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("not exceed 50 characters"));
    }

    @Test
    void testInvalidLastName_TooLong() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setLastName("a".repeat(51));

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("not exceed 50 characters"));
    }

    @Test
    void testInvalidEmail_InvalidFormat() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("invalid-email");

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("valid"));
    }

    @Test
    void testUserUpdateRequestWithRoleChanges() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setRealmRolesToAdd(Arrays.asList("admin", "manager"));
        request.setRealmRolesToRemove(Arrays.asList("user"));
        request.setClientRolesToAdd(Arrays.asList("client-admin"));
        request.setClientRolesToRemove(Arrays.asList("client-user"));

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals(Arrays.asList("admin", "manager"), request.getRealmRolesToAdd());
        assertEquals(Arrays.asList("user"), request.getRealmRolesToRemove());
        assertEquals(Arrays.asList("client-admin"), request.getClientRolesToAdd());
        assertEquals(Arrays.asList("client-user"), request.getClientRolesToRemove());
    }

    @Test
    void testUserUpdateRequestWithAttributes() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Updated");

        Map<String, java.util.List<String>> attributes = new HashMap<>();
        attributes.put("department", Arrays.asList("HR"));
        attributes.put("location", Arrays.asList("San Francisco"));
        request.setAttributes(attributes);

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals(attributes, request.getAttributes());
    }

    @Test
    void testUserUpdateRequestWithBooleanFields() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEnabled(true);
        request.setEmailVerified(false);

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
        assertTrue(request.getEnabled());
        assertFalse(request.getEmailVerified());
    }

    @Test
    void testUserUpdateRequestPartialUpdate() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Updated"); // Only update first name

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals("Updated", request.getFirstName());
        assertNull(request.getLastName());
        assertNull(request.getEmail());
    }

    @Test
    void testUserUpdateRequestMultipleValidationErrors() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("a".repeat(51)); // Invalid
        request.setLastName("a".repeat(51)); // Invalid
        request.setEmail("invalid-email"); // Invalid

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(3, violations.size());
    }

    @Test
    void testUserUpdateRequestEmptyRoleLists() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setRealmRolesToAdd(Arrays.asList());
        request.setRealmRolesToRemove(Arrays.asList());
        request.setClientRolesToAdd(Arrays.asList());
        request.setClientRolesToRemove(Arrays.asList());

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
        assertTrue(request.getRealmRolesToAdd().isEmpty());
        assertTrue(request.getRealmRolesToRemove().isEmpty());
        assertTrue(request.getClientRolesToAdd().isEmpty());
        assertTrue(request.getClientRolesToRemove().isEmpty());
    }

    @Test
    void testUserUpdateRequestNullRoleLists() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setRealmRolesToAdd(null);
        request.setRealmRolesToRemove(null);
        request.setClientRolesToAdd(null);
        request.setClientRolesToRemove(null);

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
        assertNull(request.getRealmRolesToAdd());
        assertNull(request.getRealmRolesToRemove());
        assertNull(request.getClientRolesToAdd());
        assertNull(request.getClientRolesToRemove());
    }

    @Test
    void testUserUpdateRequestEmptyAttributes() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Updated");
        request.setAttributes(new HashMap<>());

        // When
        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
        assertTrue(request.getAttributes().isEmpty());
    }
}
