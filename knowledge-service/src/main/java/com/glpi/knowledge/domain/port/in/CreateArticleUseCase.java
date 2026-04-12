package com.glpi.knowledge.domain.port.in;

import com.glpi.knowledge.domain.model.KnowbaseItem;

/**
 * Driving port — create a new KB article.
 * Requirements: 17.1
 */
public interface CreateArticleUseCase {

    KnowbaseItem createArticle(CreateArticleCommand command);
}
