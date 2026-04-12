package com.glpi.identity.domain.port.in;

/**
 * Response returned after successful authentication or token refresh.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}
