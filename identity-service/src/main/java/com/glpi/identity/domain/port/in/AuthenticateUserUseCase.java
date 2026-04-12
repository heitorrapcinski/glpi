package com.glpi.identity.domain.port.in;

/**
 * Driving port: authenticate a user and return JWT + refresh token.
 */
public interface AuthenticateUserUseCase {
    AuthResponse authenticate(AuthenticateUserCommand command);
}
