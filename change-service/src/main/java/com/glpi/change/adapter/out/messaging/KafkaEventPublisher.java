package com.glpi.change.adapter.out.messaging;

import com.glpi.change.domain.port.out.EventPublisherPort;
import com.glpi.common.DomainEventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter implementing EventPublisherPort.
 * Publishes change domain events to the changes.events topic.
 * Requirements: 11.2, 11.5, 11.7, 11.9, 21.2
 */
@Component
public class KafkaEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private static final String CHANGES_TOPIC = "changes.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEventEnvelope event) {
        log.info("Publishing event {} to topic {}", event.eventType(), CHANGES_TOPIC);
        kafkaTemplate.send(CHANGES_TOPIC, event.aggregateId(), event);
    }
}
