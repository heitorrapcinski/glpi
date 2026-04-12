package com.glpi.knowledge.domain.port.in;

import com.glpi.knowledge.domain.model.KnowbaseItem;

/**
 * Driving port — update an existing KB article with revision tracking.
 * Requirements: 17.7
 */
public interface UpdateArticleUseCase {

    KnowbaseItem updateArticle(String id, UpdateArticleCommand command);
}
