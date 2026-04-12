package com.glpi.notification.adapter.in.kafka;

import com.glpi.notification.domain.model.Actor;
import com.glpi.notification.domain.port.in.NotificationDispatchUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Kafka consumer for knowledge domain events.
 * Requirements: 16.1
 */
@Component
public class KnowledgeEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeEventConsumer.class);

    private final NotificationDispatchUseCase dispatchUseCase;

    public KnowledgeEventConsumer(NotificationDispatchUseCase dispatchUseCase) {
        this.dispatchUseCase = dispatchUseCase;
    }

    @KafkaListener(topics = "knowledge.events", groupId = "notification-service")
    public void consume(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        log.info("Received knowledge event: {}", eventType);

        // Knowledge events are consumed but no default notification templates exist for them
        // This consumer is in place for future extensibility
        log.debug("Knowledge event {} received, no notification template configured", eventType);
    }
}
