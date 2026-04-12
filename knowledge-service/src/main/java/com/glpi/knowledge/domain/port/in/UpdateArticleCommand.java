package com.glpi.knowledge.domain.port.in;

import com.glpi.knowledge.domain.model.Visibility;

import java.time.Instant;
import java.util.List;

/**
 * Command to update an existing KB article.
 */
public record UpdateArticleCommand(
        String title,
        String answer,
        String authorId,
        boolean isFaq,
        Visibility visibility,
        List<String> categoryIds,
        Instant beginDate,
        Instant endDate
) {}
