package com.glpi.identity.domain.port.in;

/**
 * Command record for assigning a profile to a user in an entity.
 */
public record AssignProfileCommand(
        String userId,
        String profileId,
        String entityId
) {}
