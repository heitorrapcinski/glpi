package com.glpi.knowledge.domain.port.out;

import com.glpi.common.DomainEventEnvelope;

/**
 * Driven port — publishes domain events to Kafka.
 * Requirements: 21.1, 21.2
 */
public interface EventPublisherPort {

    void publish(DomainEventEnvelope event);
}
