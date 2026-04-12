package com.glpi.sla.adapter.out.messaging;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.sla.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter implementing EventPublisherPort.
 * Publishes SLA domain events to the sla.events topic.
 * Requirements: 15.2, 21.2
 */
@Component
public class KafkaEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private static final String SLA_TOPIC = "sla.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEventEnvelope event) {
        log.info("Publishing event {} to topic {}", event.eventType(), SLA_TOPIC);
        kafkaTemplate.send(SLA_TOPIC, event.aggregateId(), event);
    }
}
