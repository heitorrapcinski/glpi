package com.glpi.identity.domain.model;

/**
 * Thrown when attempting to delete the last profile that holds UPDATE right on "profile" resource.
 * Maps to HTTP 409 / LAST_PROFILE_PROTECTED.
 */
public class LastProfileProtectedException extends RuntimeException {

    public LastProfileProtectedException(String profileId) {
        super("Cannot delete profile '" + profileId
                + "': it is the last profile holding UPDATE right on the 'profile' resource");
    }
}
