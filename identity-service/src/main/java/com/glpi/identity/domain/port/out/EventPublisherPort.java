package com.glpi.identity.domain.port.out;

import com.glpi.common.DomainEventEnvelope;

/**
 * Driven port: publishes domain events to the messaging infrastructure.
 */
public interface EventPublisherPort {

    void publish(DomainEventEnvelope event);
}
