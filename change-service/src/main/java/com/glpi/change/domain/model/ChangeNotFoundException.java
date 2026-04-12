package com.glpi.change.domain.model;

/**
 * Thrown when a change cannot be found by ID.
 */
public class ChangeNotFoundException extends RuntimeException {

    public ChangeNotFoundException(String id) {
        super("Change not found: " + id);
    }
}
