package com.glpi.problem.adapter.out.messaging;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.problem.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter implementing EventPublisherPort.
 * Publishes problem domain events to the problems.events topic.
 * Requirements: 10.2, 10.4, 10.6, 10.9, 21.2
 */
@Component
public class KafkaEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private static final String PROBLEMS_TOPIC = "problems.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEventEnvelope event) {
        log.info("Publishing event {} to topic {}", event.eventType(), PROBLEMS_TOPIC);
        kafkaTemplate.send(PROBLEMS_TOPIC, event.aggregateId(), event);
    }
}
