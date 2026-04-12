package com.glpi.identity.domain.port.in;

import com.glpi.identity.domain.model.EntityConfig;

import java.time.Instant;

/**
 * Response record for entity operations.
 */
public record EntityResponse(
        String id,
        String name,
        String parentId,
        int level,
        String completeName,
        EntityConfig config,
        Instant createdAt,
        Instant updatedAt
) {}
