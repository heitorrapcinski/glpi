package com.glpi.identity.domain.port.in;

/**
 * Driving port: use case for assigning a profile to a user in an entity.
 */
public interface AssignProfileUseCase {

    void assignProfile(AssignProfileCommand command);
}
