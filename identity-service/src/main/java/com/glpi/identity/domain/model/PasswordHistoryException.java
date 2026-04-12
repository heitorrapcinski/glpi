package com.glpi.identity.domain.model;

/**
 * Thrown when a password reuse from the last 5 passwords is attempted. Maps to HTTP 422.
 */
public class PasswordHistoryException extends RuntimeException {

    public PasswordHistoryException() {
        super("Password was recently used. Please choose a different password.");
    }
}
