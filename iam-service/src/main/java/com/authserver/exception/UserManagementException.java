package com.authserver.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom exception for user management operations.
 * Defaults to 500 Internal Server Error if not specified.
 */
@Getter
public class UserManagementException extends RuntimeException {

    private final HttpStatus status;
    private static final String DEFAULT_MESSAGE = "A user management error occurred.";

    /**
     * Default constructor with a generic error message and 500 Internal Server Error status.
     */
    public UserManagementException() {
        super(DEFAULT_MESSAGE);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Constructor with a custom message and 500 Internal Server Error status.
     * @param message The error message.
     */
    public UserManagementException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Constructor with a custom message and custom HTTP status.
     * @param message The error message.
     * @param status The HTTP status code.
     */
    public UserManagementException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
