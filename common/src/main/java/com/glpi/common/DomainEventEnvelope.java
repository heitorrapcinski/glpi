package com.glpi.common;

import java.time.Instant;
import java.util.Objects;

/**
 * Envelope wrapping every domain event published to Kafka.
 * All microservices must use this record when producing events to any topic.
 *
 * <p>Field semantics:
 * <ul>
 *   <li>{@code eventId}       – UUID v4 uniquely identifying this event occurrence</li>
 *   <li>{@code eventType}     – Human-readable event name, e.g. "TicketCreated"</li>
 *   <li>{@code aggregateId}   – ID of the aggregate that produced the event</li>
 *   <li>{@code aggregateType} – Type name of the aggregate, e.g. "Ticket"</li>
 *   <li>{@code occurredAt}    – UTC instant when the event occurred (ISO 8601)</li>
 *   <li>{@code version}       – Schema version, starts at 1, incremented on breaking changes</li>
 *   <li>{@code payload}       – Event-specific data object (serialized as JSON)</li>
 * </ul>
 *
 * <p>Validates: Requirements 21.2 — Property 36: Domain event envelope completeness
 */
public record DomainEventEnvelope(
        String eventId,
        String eventType,
        String aggregateId,
        String aggregateType,
        Instant occurredAt,
        int version,
        Object payload
) {
    /**
     * Compact constructor that enforces all mandatory field invariants.
     */
    public DomainEventEnvelope {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        Objects.requireNonNull(aggregateType, "aggregateType must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(payload, "payload must not be null");

        if (eventId.isBlank()) {
            throw new IllegalArgumentException("eventId must not be blank");
        }
        if (eventType.isBlank()) {
            throw new IllegalArgumentException("eventType must not be blank");
        }
        if (aggregateId.isBlank()) {
            throw new IllegalArgumentException("aggregateId must not be blank");
        }
        if (aggregateType.isBlank()) {
            throw new IllegalArgumentException("aggregateType must not be blank");
        }
        if (version < 1) {
            throw new IllegalArgumentException("version must be a positive integer (>= 1)");
        }
    }
}
