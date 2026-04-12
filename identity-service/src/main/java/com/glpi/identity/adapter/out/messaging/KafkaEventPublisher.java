package com.glpi.identity.adapter.out.messaging;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.identity.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter implementing the EventPublisherPort.
 * Publishes domain events to the configured Kafka topic.
 */
@Component
public class KafkaEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEventEnvelope event) {
        String topic = resolveTopic(event.aggregateType());
        log.info("Publishing event {} to topic {}", event.eventType(), topic);
        kafkaTemplate.send(topic, event.aggregateId(), event);
    }

    private String resolveTopic(String aggregateType) {
        return switch (aggregateType) {
            case "User" -> "identity.users";
            case "Entity" -> "identity.entities";
            case "Profile" -> "identity.profiles";
            default -> "identity.events";
        };
    }
}
