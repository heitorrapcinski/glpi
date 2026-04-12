package com.glpi.sla.application.usecase;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.sla.adapter.out.persistence.EscalationExecutionDocument;
import com.glpi.sla.adapter.out.persistence.MongoEscalationExecutionRepository;
import com.glpi.sla.domain.model.Sla;
import com.glpi.sla.domain.model.SlaLevel;
import com.glpi.sla.domain.port.out.EventPublisherPort;
import com.glpi.sla.domain.port.out.SlaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Scheduled escalation evaluator.
 * Runs every 5 minutes (configurable), queries active tickets from Ticket Service,
 * evaluates SLA levels, and publishes SlaEscalationTriggered events.
 * Requirements: 15.1, 15.2, 15.6
 */
@Component
public class EscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(EscalationScheduler.class);

    private final SlaRepository slaRepository;
    private final EventPublisherPort eventPublisher;
    private final MongoEscalationExecutionRepository executionRepository;
    private final RestClient restClient;

    public EscalationScheduler(
            SlaRepository slaRepository,
            EventPublisherPort eventPublisher,
            MongoEscalationExecutionRepository executionRepository,
            @Value("${ticket.service.url:http://localhost:8082}") String ticketServiceUrl) {
        this.slaRepository = slaRepository;
        this.eventPublisher = eventPublisher;
        this.executionRepository = executionRepository;
        this.restClient = RestClient.builder()
                .baseUrl(ticketServiceUrl)
                .build();
    }

    @Scheduled(cron = "${escalation.schedule.cron:0 */5 * * * *}")
    public void evaluateEscalations() {
        log.debug("Escalation scheduler triggered at {}", Instant.now());

        List<Map<String, Object>> activeTickets = fetchActiveTickets();
        if (activeTickets == null || activeTickets.isEmpty()) {
            log.debug("No active tickets found for escalation evaluation");
            return;
        }

        for (Map<String, Object> ticket : activeTickets) {
            try {
                evaluateTicket(ticket);
            } catch (Exception e) {
                log.warn("Error evaluating escalation for ticket {}: {}", ticket.get("id"), e.getMessage());
            }
        }
    }

    private List<Map<String, Object>> fetchActiveTickets() {
        try {
            return restClient.get()
                    .uri("/tickets?status=1,2,3&size=1000")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .entrySet().stream()
                    .filter(e -> "content".equals(e.getKey()))
                    .map(e -> (List<Map<String, Object>>) e.getValue())
                    .findFirst()
                    .orElse(List.of());
        } catch (Exception e) {
            log.warn("Could not fetch active tickets from Ticket Service: {}", e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private void evaluateTicket(Map<String, Object> ticket) {
        String ticketId = (String) ticket.get("id");
        Map<String, Object> slaCtx = (Map<String, Object>) ticket.get("sla");
        if (slaCtx == null) return;

        String slaId = (String) slaCtx.get("slaId");
        if (slaId == null) return;

        Optional<Sla> slaOpt = slaRepository.findById(slaId);
        if (slaOpt.isEmpty()) return;

        Sla sla = slaOpt.get();
        Instant now = Instant.now();

        // Determine the relevant deadline based on SLA type
        String deadlineStr = switch (sla.getType()) {
            case TTO -> (String) slaCtx.get("ttoDeadline");
            case TTR -> (String) slaCtx.get("ttrDeadline");
        };
        if (deadlineStr == null) return;

        Instant deadline = Instant.parse(deadlineStr);

        for (SlaLevel level : sla.getLevels()) {
            // Trigger time = deadline + executionDelaySeconds
            // (negative delay = before deadline, positive = after)
            Instant triggerTime = deadline.plusSeconds(level.getExecutionDelaySeconds());

            if (now.isAfter(triggerTime) || now.equals(triggerTime)) {
                if (!executionRepository.existsByTicketIdAndSlaIdAndLevelId(ticketId, slaId, level.getId())) {
                    publishEscalationEvent(ticketId, slaId, level);
                    recordExecution(ticketId, slaId, level.getId());
                }
            }
        }
    }

    private void publishEscalationEvent(String ticketId, String slaId, SlaLevel level) {
        Map<String, Object> payload = Map.of(
                "ticketId", ticketId,
                "slaId", slaId,
                "levelId", level.getId(),
                "actions", level.getActions() != null ? level.getActions() : List.of()
        );

        DomainEventEnvelope event = new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "SlaEscalationTriggered",
                ticketId,
                "Ticket",
                Instant.now(),
                1,
                payload
        );

        eventPublisher.publish(event);
        log.info("SlaEscalationTriggered for ticket={} sla={} level={}", ticketId, slaId, level.getId());
    }

    private void recordExecution(String ticketId, String slaId, String levelId) {
        EscalationExecutionDocument exec = new EscalationExecutionDocument(
                UUID.randomUUID().toString(),
                ticketId,
                slaId,
                levelId,
                Instant.now()
        );
        executionRepository.save(exec);
    }
}
