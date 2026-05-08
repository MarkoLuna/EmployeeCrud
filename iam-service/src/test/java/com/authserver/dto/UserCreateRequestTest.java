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

class UserCreateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidUserCreateRequest() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidUsername_Empty() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(2, violations.size()); // Both @NotBlank and @Size constraints are triggered
        boolean foundNotBlank = violations.stream()
            .anyMatch(v -> v.getMessage().equals("Username is required"));
        assertTrue(foundNotBlank, "Should contain 'Username is required' violation");
    }

    @Test
    void testInvalidUsername_TooShort() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("ab");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("between 3 and 50 characters"));
    }

    @Test
    void testInvalidUsername_TooLong() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("a".repeat(51));
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("between 3 and 50 characters"));
    }

    @Test
    void testInvalidFirstName_Empty() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("First name is required", violations.iterator().next().getMessage());
    }

    @Test
    void testInvalidFirstName_TooLong() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("a".repeat(51));
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("not exceed 50 characters"));
    }

    @Test
    void testInvalidLastName_Empty() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("Last name is required", violations.iterator().next().getMessage());
    }

    @Test
    void testInvalidEmail_Empty() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertEquals("Email is required", violations.iterator().next().getMessage());
    }

    @Test
    void testInvalidEmail_InvalidFormat() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("invalid-email");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("valid"));
    }

    @Test
    void testInvalidPassword_Empty() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("");

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(2, violations.size()); // Both @NotBlank and @Size constraints are triggered
        boolean foundNotBlank = violations.stream()
            .anyMatch(v -> v.getMessage().equals("Password is required"));
        assertTrue(foundNotBlank, "Should contain 'Password is required' violation");
    }

    @Test
    void testInvalidPassword_TooShort() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("123");

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("at least 6 characters"));
    }

    @Test
    void testUserCreateRequestWithRoles() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRealmRoles(Arrays.asList("user", "admin"));
        request.setClientRoles(Arrays.asList("client-admin"));

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals(Arrays.asList("user", "admin"), request.getRealmRoles());
        assertEquals(Arrays.asList("client-admin"), request.getClientRoles());
    }

    @Test
    void testUserCreateRequestWithAttributes() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        Map<String, java.util.List<String>> attributes = new HashMap<>();
        attributes.put("department", Arrays.asList("IT"));
        attributes.put("location", Arrays.asList("New York"));
        request.setAttributes(attributes);

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals(attributes, request.getAttributes());
    }

    @Test
    void testUserCreateRequestDefaultValues() {
        // Given
        UserCreateRequest request = new UserCreateRequest();

        // When
        boolean defaultEnabled = request.isEnabled();
        boolean defaultEmailVerified = request.isEmailVerified();

        // Then
        assertTrue(defaultEnabled);
        assertFalse(defaultEmailVerified);
    }

    @Test
    void testMultipleValidationErrors() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(""); // Invalid
        request.setFirstName(""); // Invalid
        request.setLastName(""); // Invalid
        request.setEmail("invalid-email"); // Invalid
        request.setPassword("123"); // Invalid

        // When
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);

        // When & Then
        assertEquals(6, violations.size()); // Each empty field triggers both @NotBlank and @Size constraints
    }
}
