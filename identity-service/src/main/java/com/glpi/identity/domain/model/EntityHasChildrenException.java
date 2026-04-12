package com.glpi.identity.domain.model;

/**
 * Thrown when attempting to delete an entity that still has child entities.
 * Maps to HTTP 409 / ENTITY_HAS_CHILDREN.
 */
public class EntityHasChildrenException extends RuntimeException {

    public EntityHasChildrenException(String entityId) {
        super("Entity '" + entityId + "' cannot be deleted because it has child entities");
    }
}
