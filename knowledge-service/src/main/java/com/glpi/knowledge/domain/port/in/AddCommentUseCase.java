package com.glpi.knowledge.domain.port.in;

import com.glpi.knowledge.domain.model.KnowbaseItem;

/**
 * Driving port — add a comment to a KB article.
 * Requirements: 17.8
 */
public interface AddCommentUseCase {

    KnowbaseItem addComment(String articleId, AddCommentCommand command);
}
