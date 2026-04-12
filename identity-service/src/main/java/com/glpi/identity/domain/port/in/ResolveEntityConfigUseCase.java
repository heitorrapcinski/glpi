package com.glpi.identity.domain.port.in;

import com.glpi.identity.domain.model.EntityConfig;

/**
 * Driving port: use case for resolving the effective entity configuration
 * by traversing the entity tree upward for CONFIG_PARENT (-2) fields.
 */
public interface ResolveEntityConfigUseCase {

    EntityConfig resolveConfig(String entityId);
}
