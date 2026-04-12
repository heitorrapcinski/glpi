package com.glpi.notification.adapter.in.kafka;

import com.glpi.notification.domain.model.Actor;
import com.glpi.notification.domain.port.in.NotificationDispatchUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Kafka consumer for change domain events.
 * Requirements: 16.1, 16.2
 */
@Component
public class ChangeEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ChangeEventConsumer.class);

    private final NotificationDispatchUseCase dispatchUseCase;

    public ChangeEventConsumer(NotificationDispatchUseCase dispatchUseCase) {
        this.dispatchUseCase = dispatchUseCase;
    }

    @KafkaListener(topics = "changes.events", groupId = "notification-service")
    public void consume(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        log.info("Received change event: {}", eventType);

        String mappedEventType = mapEventType(eventType);
        if (mappedEventType == null) {
            log.debug("Ignoring unmapped change event type: {}", eventType);
            return;
        }

        List<Actor> actors = EventConsumerHelper.extractActors(event);
        Map<String, Object> context = EventConsumerHelper.extractContext(event);
        dispatchUseCase.dispatch(mappedEventType, actors, context);
    }

    private String mapEventType(String eventType) {
        if (eventType == null) return null;
        return switch (eventType) {
            case "ChangeCreated" -> "change.created";
            case "ChangeValidationApproved" -> "change.validation.approved";
            default -> null;
        };
    }
}
