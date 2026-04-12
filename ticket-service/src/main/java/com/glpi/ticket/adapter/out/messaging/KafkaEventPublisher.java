package com.glpi.ticket.adapter.out.messaging;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter implementing EventPublisherPort.
 * Publishes ticket domain events to the tickets.events topic.
 * Requirements: 5.10, 21.2
 */
@Component
public class KafkaEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private static final String TICKETS_TOPIC = "tickets.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEventEnvelope event) {
        log.info("Publishing event {} to topic {}", event.eventType(), TICKETS_TOPIC);
        kafkaTemplate.send(TICKETS_TOPIC, event.aggregateId(), event);
    }
}
