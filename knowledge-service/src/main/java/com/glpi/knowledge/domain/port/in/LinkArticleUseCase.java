package com.glpi.knowledge.domain.port.in;

import com.glpi.knowledge.domain.model.KnowbaseItem;

/**
 * Driving port — link a KB article to another ITIL item.
 * Requirements: 17.11
 */
public interface LinkArticleUseCase {

    KnowbaseItem linkArticle(String articleId, LinkArticleCommand command);
}
