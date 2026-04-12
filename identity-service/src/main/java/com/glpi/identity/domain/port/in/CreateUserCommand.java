package com.glpi.identity.domain.port.in;

import com.glpi.identity.domain.model.AuthType;
import com.glpi.identity.domain.model.Email;

import java.util.List;

/**
 * Command object for creating a new user.
 */
public record CreateUserCommand(
        String username,
        String password,
        AuthType authType,
        List<Email> emails,
        String entityId,
        String profileId
) {}
