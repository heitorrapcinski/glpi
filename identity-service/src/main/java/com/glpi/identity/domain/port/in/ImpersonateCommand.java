package com.glpi.identity.domain.port.in;

/**
 * Command for impersonating a target user on behalf of a requesting user.
 */
public record ImpersonateCommand(String requestingUserId, String targetUserId) {}
