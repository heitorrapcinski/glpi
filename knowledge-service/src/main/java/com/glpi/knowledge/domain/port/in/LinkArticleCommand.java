package com.glpi.knowledge.domain.port.in;

/**
 * Command to link a KB article to another ITIL item.
 */
public record LinkArticleCommand(
        String itemType,
        String itemId
) {}
