package com.glpi.identity.domain.model;

/**
 * Thrown when an entity cannot be found by its ID.
 * Maps to HTTP 404 / ENTITY_NOT_FOUND.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityId) {
        super("Entity not found: " + entityId);
    }
}
