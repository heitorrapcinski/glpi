package com.glpi.knowledge.domain.model;

/**
 * Thrown when a KB article is not found.
 */
public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(String id) {
        super("Knowledge article not found: " + id);
    }
}
