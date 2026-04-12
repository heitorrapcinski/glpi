package com.glpi.identity.domain.model;

/**
 * Thrown when an entity with the same name already exists under the same parent.
 * Maps to HTTP 409 / DUPLICATE_ENTITY_NAME.
 */
public class DuplicateEntityNameException extends RuntimeException {

    public DuplicateEntityNameException(String name, String parentId) {
        super("Entity with name '" + name + "' already exists under parent '" + parentId + "'");
    }
}
