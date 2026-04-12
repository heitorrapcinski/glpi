package com.glpi.identity.domain.port.in;

import com.glpi.identity.domain.model.AuthType;
import com.glpi.identity.domain.model.Email;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for user operations. Excludes sensitive fields
 * (passwordHash, personalToken, apiToken, totpSecret).
 */
public record UserResponse(
        String id,
        String username,
        AuthType authType,
        String authSourceId,
        List<Email> emails,
        boolean isActive,
        boolean isDeleted,
        String entityId,
        String profileId,
        String language,
        boolean twoFactorEnabled,
        int failedLoginAttempts,
        Instant lockedUntil,
        Instant createdAt,
        Instant updatedAt
) {}
