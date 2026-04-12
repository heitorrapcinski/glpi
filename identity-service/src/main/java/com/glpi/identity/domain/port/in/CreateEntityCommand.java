package com.glpi.identity.domain.port.in;

import com.glpi.identity.domain.model.EntityConfig;

/**
 * Command record for creating a new entity.
 */
public record CreateEntityCommand(
        String name,
        String parentId,
        EntityConfig config
) {}
