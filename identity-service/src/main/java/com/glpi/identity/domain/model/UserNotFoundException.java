package com.glpi.identity.domain.model;

/**
 * Thrown when a requested user does not exist. Maps to HTTP 404.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String id) {
        super("User not found: " + id);
    }
}
