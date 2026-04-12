package com.glpi.notification.adapter.in.kafka;

import com.glpi.notification.domain.model.Actor;
import com.glpi.notification.domain.port.in.NotificationDispatchUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Kafka consumer for SLA domain events.
 * Requirements: 15.3, 16.1, 16.2
 */
@Component
public class SlaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SlaEventConsumer.class);

    private final NotificationDispatchUseCase dispatchUseCase;

    public SlaEventConsumer(NotificationDispatchUseCase dispatchUseCase) {
        this.dispatchUseCase = dispatchUseCase;
    }

    @KafkaListener(topics = "sla.events", groupId = "notification-service")
    public void consume(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        log.info("Received SLA event: {}", eventType);

        if ("SlaEscalationTriggered".equals(eventType)) {
            List<Actor> actors = EventConsumerHelper.extractActors(event);
            Map<String, Object> context = EventConsumerHelper.extractContext(event);
            dispatchUseCase.dispatch("sla.escalation.triggered", actors, context);
        } else {
            log.debug("Ignoring unmapped SLA event type: {}", eventType);
        }
    }
}
