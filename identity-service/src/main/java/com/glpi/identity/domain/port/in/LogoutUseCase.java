package com.glpi.identity.domain.port.in;

/**
 * Driving port: log out a user by blocklisting their current JWT.
 */
public interface LogoutUseCase {
    void logout(String accessToken);
}
