package com.glpi.ticket.domain.port.out;

import com.glpi.common.DomainEventEnvelope;

/**
 * Driven port — domain event publication contract.
 * Requirements: 21.2
 */
public interface EventPublisherPort {

    void publish(DomainEventEnvelope event);
}
