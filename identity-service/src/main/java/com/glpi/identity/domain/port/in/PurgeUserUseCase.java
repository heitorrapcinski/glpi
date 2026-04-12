package com.glpi.identity.domain.port.in;

/**
 * Driving port: use case for permanently purging a user from the system.
 */
public interface PurgeUserUseCase {

    void purgeUser(String userId);
}
