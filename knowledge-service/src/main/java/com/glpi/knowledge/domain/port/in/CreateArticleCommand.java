package com.glpi.knowledge.domain.port.in;

import com.glpi.knowledge.domain.model.Visibility;

import java.time.Instant;
import java.util.List;

/**
 * Command to create a new KB article.
 */
public record CreateArticleCommand(
        String title,
        String answer,
        String authorId,
        boolean isFaq,
        Visibility visibility,
        List<String> categoryIds,
        Instant beginDate,
        Instant endDate
) {}
