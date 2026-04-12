package com.glpi.identity.domain.model;

/**
 * Thrown when a profile cannot be found by its ID.
 * Maps to HTTP 404 / PROFILE_NOT_FOUND.
 */
public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(String profileId) {
        super("Profile not found: " + profileId);
    }
}
