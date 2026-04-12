package com.glpi.ticket.adapter.in.kafka;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.Actor;
import com.glpi.ticket.domain.model.ActorType;
import com.glpi.ticket.domain.model.Ticket;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka consumer for SLA escalation events from the sla.events topic.
 * Handles "reassign" and "change_priority" escalation actions.
 * Requirements: 15.4, 15.5
 */
@Component
public class SlaEscalationConsumer {

    private static final Logger log = LoggerFactory.getLogger(SlaEscalationConsumer.class);

    private final TicketRepository ticketRepository;
    private final EventPublisherPort eventPublisher;

    public SlaEscalationConsumer(TicketRepository ticketRepository,
                                  EventPublisherPort eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    @SuppressWarnings("unchecked")
    @KafkaListener(topics = "sla.events", groupId = "ticket-service")
    public void consume(Map<String, Object> envelope) {
        String eventType = (String) envelope.get("eventType");
        if (!"SlaEscalationTriggered".equals(eventType)) {
            return;
        }

        log.info("Received SlaEscalationTriggered event");

        Map<String, Object> payload = (Map<String, Object>) envelope.get("payload");
        if (payload == null) {
            log.warn("SlaEscalationTriggered event has no payload, skipping");
            return;
        }

        String ticketId = (String) payload.get("ticketId");
        List<Map<String, Object>> actions = (List<Map<String, Object>>) payload.get("actions");

        if (ticketId == null || actions == null) {
            log.warn("SlaEscalationTriggered event missing ticketId or actions, skipping");
            return;
        }

        ticketRepository.findById(ticketId).ifPresentOrElse(
                ticket -> processActions(ticket, actions),
                () -> log.warn("Ticket {} not found for SLA escalation", ticketId)
        );
    }

    @SuppressWarnings("unchecked")
    private void processActions(Ticket ticket, List<Map<String, Object>> actions) {
        boolean modified = false;

        for (Map<String, Object> action : actions) {
            String actionType = (String) action.get("actionType");
            Map<String, Object> parameters = (Map<String, Object>) action.getOrDefault("parameters", Map.of());

            switch (actionType) {
                case "reassign" -> {
                    modified = handleReassign(ticket, parameters);
                }
                case "change_priority" -> {
                    modified = handleChangePriority(ticket, parameters);
                }
                default -> log.debug("Ignoring escalation action type: {}", actionType);
            }
        }

        if (modified) {
            ticket.setUpdatedAt(Instant.now());
            ticketRepository.save(ticket);

            eventPublisher.publish(new DomainEventEnvelope(
                    UUID.randomUUID().toString(),
                    "TicketUpdated",
                    ticket.getId(),
                    "Ticket",
                    Instant.now(),
                    1,
                    ticket
            ));
        }
    }

    private boolean handleReassign(Ticket ticket, Map<String, Object> parameters) {
        String groupId = (String) parameters.get("groupId");
        String userId = (String) parameters.get("userId");

        if (groupId != null) {
            // Remove existing assigned groups and add the new one
            ticket.getActors().removeIf(a ->
                    a.getActorType() == ActorType.ASSIGNED && "group".equals(a.getActorKind()));
            ticket.getActors().add(new Actor(ActorType.ASSIGNED, "group", groupId, true));
            log.info("Reassigned ticket {} to group {}", ticket.getId(), groupId);
            return true;
        }

        if (userId != null) {
            // Remove existing assigned users and add the new one
            ticket.getActors().removeIf(a ->
                    a.getActorType() == ActorType.ASSIGNED && "user".equals(a.getActorKind()));
            ticket.getActors().add(new Actor(ActorType.ASSIGNED, "user", userId, true));
            log.info("Reassigned ticket {} to user {}", ticket.getId(), userId);
            return true;
        }

        return false;
    }

    private boolean handleChangePriority(Ticket ticket, Map<String, Object> parameters) {
        Object priorityObj = parameters.get("priority");
        if (priorityObj instanceof Number priority) {
            // System-initiated priority change bypasses CHANGEPRIORITY right check
            ticket.setPriority(priority.intValue());
            log.info("Changed priority of ticket {} to {} (SLA escalation)", ticket.getId(), priority);
            return true;
        }
        return false;
    }
}
