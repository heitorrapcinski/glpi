package com.glpi.identity.domain.model;

/**
 * Email value object associated with a User.
 */
public record Email(String email, boolean isDefault) {

    public Email {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email address must not be null or blank");
        }
    }
}
