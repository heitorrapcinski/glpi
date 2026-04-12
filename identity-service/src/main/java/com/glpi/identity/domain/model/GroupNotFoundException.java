package com.glpi.identity.domain.model;

/**
 * Thrown when a group cannot be found by its ID.
 * Maps to HTTP 404 / GROUP_NOT_FOUND.
 */
public class GroupNotFoundException extends RuntimeException {

    public GroupNotFoundException(String groupId) {
        super("Group not found: " + groupId);
    }
}
