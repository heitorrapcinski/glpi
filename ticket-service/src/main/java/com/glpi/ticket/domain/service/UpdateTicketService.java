package com.glpi.ticket.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.InsufficientRightsException;
import com.glpi.ticket.domain.model.SlaContext;
import com.glpi.ticket.domain.model.Ticket;
import com.glpi.ticket.domain.model.TicketNotFoundException;
import com.glpi.ticket.domain.model.TicketStatus;
import com.glpi.ticket.domain.port.in.UpdateTicketCommand;
import com.glpi.ticket.domain.port.in.UpdateTicketUseCase;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing UpdateTicketUseCase.
 * Handles SLA timer pause/resume on WAITING transitions.
 * Requirements: 5.8, 5.9, 5.10, 9.3, 9.4, 9.5, 27.1, 27.2, 27.3, 27.4
 */
@Service
public class UpdateTicketService implements UpdateTicketUseCase {

    /** Bit 65536 — CHANGEPRIORITY right on "ticket" resource */
    private static final int CHANGEPRIORITY_BIT = 65536;

    private final TicketRepository ticketRepository;
    private final EventPublisherPort eventPublisher;
    private final PriorityMatrixService priorityMatrixService;

    public UpdateTicketService(TicketRepository ticketRepository,
                               EventPublisherPort eventPublisher,
                               PriorityMatrixService priorityMatrixService) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
        this.priorityMatrixService = priorityMatrixService;
    }

    @Override
    public Ticket updateTicket(UpdateTicketCommand cmd) {
        Ticket ticket = ticketRepository.findById(cmd.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(cmd.ticketId()));

        if (cmd.title() != null) ticket.setTitle(cmd.title());
        if (cmd.content() != null) ticket.setContent(cmd.content());
        if (cmd.type() != null) ticket.setType(cmd.type());
        if (cmd.categoryId() != null) ticket.setCategoryId(cmd.categoryId());

        boolean urgencyChanged = cmd.urgency() != null && cmd.urgency() != ticket.getUrgency();
        boolean impactChanged = cmd.impact() != null && cmd.impact() != ticket.getImpact();

        if (cmd.urgency() != null) ticket.setUrgency(cmd.urgency());
        if (cmd.impact() != null) ticket.setImpact(cmd.impact());

        // Direct priority override requires CHANGEPRIORITY right
        if (cmd.priority() != null) {
            if ((cmd.userRights() & CHANGEPRIORITY_BIT) == 0) {
                throw new InsufficientRightsException("CHANGEPRIORITY");
            }
            ticket.setPriority(cmd.priority());
            ticket.setPriorityManualOverride(true);
        } else if ((urgencyChanged || impactChanged) && !ticket.isPriorityManualOverride()) {
            // Auto-recompute priority when urgency/impact changes
            int newPriority = priorityMatrixService.computePriority(
                    ticket.getUrgency(), ticket.getImpact(), ticket.getEntityId());
            ticket.setPriority(newPriority);
        }

        // Handle status transition with SLA timer logic
        if (cmd.status() != null && cmd.status() != ticket.getStatus()) {
            TicketStatus previousStatus = ticket.getStatus();
            TicketStatus newStatus = cmd.status();
            Instant now = Instant.now();

            handleSlaTimerTransition(ticket, previousStatus, newStatus, now);

            // Record TTO (takeIntoAccountDelay) when ticket is first assigned
            if (newStatus == TicketStatus.ASSIGNED && ticket.getTakeIntoAccountDelay() == null
                    && ticket.getCreatedAt() != null) {
                ticket.setTakeIntoAccountDelay(now.getEpochSecond() - ticket.getCreatedAt().getEpochSecond());
            }

            ticket.setStatus(newStatus);
        }

        Instant now = Instant.now();
        ticket.setUpdatedAt(now);

        Ticket saved = ticketRepository.save(ticket);

        DomainEventEnvelope event = new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "TicketUpdated",
                saved.getId(),
                "Ticket",
                now,
                1,
                saved
        );
        eventPublisher.publish(event);

        return saved;
    }

    /**
     * Pause SLA timer when entering WAITING; resume and extend deadlines when exiting.
     */
    private void handleSlaTimerTransition(Ticket ticket, TicketStatus from, TicketStatus to, Instant now) {
        SlaContext sla = ticket.getSla();
        if (sla == null) return;

        if (to == TicketStatus.WAITING) {
            // Pause: record waiting start
            sla.setWaitingStart(now);
        } else if (from == TicketStatus.WAITING && sla.getWaitingStart() != null) {
            // Resume: compute elapsed, extend deadlines
            long elapsed = now.getEpochSecond() - sla.getWaitingStart().getEpochSecond();
            sla.setWaitingDuration(sla.getWaitingDuration() + elapsed);

            if (sla.getTtoDeadline() != null) {
                sla.setTtoDeadline(sla.getTtoDeadline().plusSeconds(elapsed));
            }
            if (sla.getTtrDeadline() != null) {
                sla.setTtrDeadline(sla.getTtrDeadline().plusSeconds(elapsed));
            }
            if (sla.getInternalTtoDeadline() != null) {
                sla.setInternalTtoDeadline(sla.getInternalTtoDeadline().plusSeconds(elapsed));
            }
            if (sla.getInternalTtrDeadline() != null) {
                sla.setInternalTtrDeadline(sla.getInternalTtrDeadline().plusSeconds(elapsed));
            }
            sla.setWaitingStart(null);
        }
    }
}
