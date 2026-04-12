package com.glpi.identity.domain.port.in;

/**
 * Driving port: use case for deactivating a user account.
 */
public interface DeactivateUserUseCase {

    void deactivateUser(String userId);
}
