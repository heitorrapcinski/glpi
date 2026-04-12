package com.glpi.knowledge.domain.port.in;

/**
 * Command to add a comment to a KB article.
 */
public record AddCommentCommand(
        String content,
        String authorId
) {}
