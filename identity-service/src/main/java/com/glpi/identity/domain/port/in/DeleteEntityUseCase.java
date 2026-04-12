package com.glpi.identity.domain.port.in;

/**
 * Driving port: use case for deleting an entity.
 */
public interface DeleteEntityUseCase {

    void deleteEntity(String entityId);
}
