package com.glpi.notification.adapter.in.kafka;

import com.glpi.notification.domain.model.Actor;
import com.glpi.notification.domain.port.in.NotificationDispatchUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Kafka consumer for ticket domain events.
 * Consumer group: notification-service
 * Requirements: 16.1, 16.2, 21.5
 */
@Component
public class TicketEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketEventConsumer.class);

    private final NotificationDispatchUseCase dispatchUseCase;

    public TicketEventConsumer(NotificationDispatchUseCase dispatchUseCase) {
        this.dispatchUseCase = dispatchUseCase;
    }

    @KafkaListener(topics = "tickets.events", groupId = "notification-service")
    public void consume(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        log.info("Received ticket event: {}", eventType);

        String mappedEventType = mapEventType(eventType);
        if (mappedEventType == null) {
            log.debug("Ignoring unmapped ticket event type: {}", eventType);
            return;
        }

        List<Actor> actors = extractActors(event);
        Map<String, Object> context = extractContext(event);
        dispatchUseCase.dispatch(mappedEventType, actors, context);
    }

    private String mapEventType(String eventType) {
        if (eventType == null) return null;
        return switch (eventType) {
            case "TicketCreated" -> "ticket.created";
            case "TicketUpdated" -> "ticket.updated";
            case "TicketSolved" -> "ticket.solved";
            case "TicketClosed" -> "ticket.closed";
            case "TicketDeleted" -> "ticket.deleted";
            case "TicketValidationRequested" -> "ticket.validation.requested";
            case "TicketValidationApproved" -> "ticket.validation.approved";
            case "TicketValidationRefused" -> "ticket.validation.refused";
            default -> null;
        };
    }

    private List<Actor> extractActors(Map<String, Object> event) {
        return EventConsumerHelper.extractActors(event);
    }

    private Map<String, Object> extractContext(Map<String, Object> event) {
        return EventConsumerHelper.extractContext(event);
    }
}
