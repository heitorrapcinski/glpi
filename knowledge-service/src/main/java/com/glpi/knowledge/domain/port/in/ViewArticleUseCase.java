package com.glpi.knowledge.domain.port.in;

/**
 * Driving port — view an article and increment view counter atomically.
 * Requirements: 17.9
 */
public interface ViewArticleUseCase {

    void viewArticle(String id);
}
