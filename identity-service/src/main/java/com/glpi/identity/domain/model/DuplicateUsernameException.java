package com.glpi.identity.domain.model;

/**
 * Thrown when a user creation is attempted with a username that already exists
 * for the same authentication type. Maps to HTTP 409.
 */
public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String username) {
        super("Username already exists: " + username);
    }
}
