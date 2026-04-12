package com.glpi.sla.domain.port.out;

import com.glpi.common.DomainEventEnvelope;

/**
 * Driven port for publishing domain events to Kafka.
 * Requirements: 15.2, 21.2
 */
public interface EventPublisherPort {
    void publish(DomainEventEnvelope event);
}
