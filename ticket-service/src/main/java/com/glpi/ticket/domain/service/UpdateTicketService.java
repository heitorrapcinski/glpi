package com.glpi.ticket.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.InsufficientRightsException;
import com.glpi.ticket.domain.model.Ticket;
import com.glpi.ticket.domain.model.TicketNotFoundException;
import com.glpi.ticket.domain.port.in.UpdateTicketCommand;
import com.glpi.ticket.domain.port.in.UpdateTicketUseCase;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing UpdateTicketUseCase.
 * Requirements: 5.8, 5.9, 5.10, 27.1, 27.2, 27.3, 27.4
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
        if (cmd.status() != null) ticket.setStatus(cmd.status());
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

        ticket.setUpdatedAt(Instant.now());

        Ticket saved = ticketRepository.save(ticket);

        DomainEventEnvelope event = new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "TicketUpdated",
                saved.getId(),
                "Ticket",
                Instant.now(),
                1,
                saved
        );
        eventPublisher.publish(event);

        return saved;
    }
}
