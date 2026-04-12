package com.glpi.knowledge.adapter.out.messaging;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.knowledge.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter implementing EventPublisherPort — publishes domain events to knowledge.events topic.
 * Requirements: 21.1, 21.2
 */
@Component
public class KafkaEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private static final String TOPIC = "knowledge.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEventEnvelope event) {
        kafkaTemplate.send(TOPIC, event.aggregateId(), event);
        log.info("Published {} event for aggregate {} to {}", event.eventType(), event.aggregateId(), TOPIC);
    }
}
