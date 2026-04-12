package com.glpi.identity.domain.port.in;

/**
 * Driving port: use case for creating a new entity.
 */
public interface CreateEntityUseCase {

    EntityResponse createEntity(CreateEntityCommand command);
}
