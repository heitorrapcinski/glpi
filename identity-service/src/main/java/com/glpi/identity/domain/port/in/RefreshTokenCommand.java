package com.glpi.identity.domain.port.in;

/**
 * Command for refreshing an access token using a valid refresh token.
 */
public record RefreshTokenCommand(String refreshToken) {}
