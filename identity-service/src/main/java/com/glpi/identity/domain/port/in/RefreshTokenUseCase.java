package com.glpi.identity.domain.port.in;

/**
 * Driving port: exchange a valid refresh token for a new access + refresh token pair.
 */
public interface RefreshTokenUseCase {
    AuthResponse refresh(RefreshTokenCommand command);
}
