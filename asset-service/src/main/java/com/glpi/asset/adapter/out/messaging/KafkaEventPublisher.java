package com.glpi.asset.adapter.out.messaging;

import com.glpi.asset.domain.port.out.EventPublisherPort;
import com.glpi.common.DomainEventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter implementing EventPublisherPort.
 * Publishes asset domain events to the assets.events topic.
 * Requirements: 12.5, 12.6, 12.7, 13.4, 21.2
 */
@Component
public class KafkaEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private static final String ASSETS_TOPIC = "assets.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEventEnvelope event) {
        log.info("Publishing event {} to topic {}", event.eventType(), ASSETS_TOPIC);
        kafkaTemplate.send(ASSETS_TOPIC, event.aggregateId(), event);
    }
}
