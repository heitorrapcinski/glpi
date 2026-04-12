package com.glpi.knowledge.domain.port.in;

/**
 * Command to create a new KB category.
 */
public record CreateCategoryCommand(
        String name,
        String parentId
) {}
