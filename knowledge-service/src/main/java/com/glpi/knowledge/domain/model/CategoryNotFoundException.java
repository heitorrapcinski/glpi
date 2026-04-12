package com.glpi.knowledge.domain.model;

/**
 * Thrown when a KB category is not found.
 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String id) {
        super("Knowledge category not found: " + id);
    }
}
