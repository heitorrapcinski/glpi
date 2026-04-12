package com.glpi.ticket.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.*;
import com.glpi.ticket.domain.port.in.AssignTicketCommand;
import com.glpi.ticket.domain.port.in.AssignTicketUseCase;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing AssignTicketUseCase.
 * Transitions INCOMING → ASSIGNED and enforces OWN/STEAL rights.
 * Requirements: 5.4, 6.5, 6.6, 26.1, 26.5, 26.6
 */
@Service
public class AssignTicketService implements AssignTicketUseCase {

    /** Bit 32768 — OWN right (self-assignment when no assigned user exists) */
    private static final int OWN_BIT = 32768;
    /** Bit 16384 — STEAL right (reassignment from another user) */
    private static final int STEAL_BIT = 16384;

    private final TicketRepository ticketRepository;
    private final EventPublisherPort eventPublisher;
    private final StatusTransitionService statusTransitionService;

    public AssignTicketService(TicketRepository ticketRepository,
                               EventPublisherPort eventPublisher,
                               StatusTransitionService statusTransitionService) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
        this.statusTransitionService = statusTransitionService;
    }

    @Override
    public Ticket assignTicket(AssignTicketCommand cmd) {
        Ticket ticket = ticketRepository.findById(cmd.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(cmd.ticketId()));

        boolean alreadyAssigned = ticket.getActors().stream()
                .anyMatch(a -> a.getActorType() == ActorType.ASSIGNED);

        if (alreadyAssigned) {
            // Reassignment requires STEAL right
            if ((cmd.userRights() & STEAL_BIT) == 0) {
                throw new InsufficientRightsException("STEAL");
            }
            // Remove existing assigned actors
            ticket.getActors().removeIf(a -> a.getActorType() == ActorType.ASSIGNED);
        } else {
            // Self-assignment with no existing assignee requires OWN right
            if ((cmd.userRights() & OWN_BIT) == 0) {
                throw new InsufficientRightsException("OWN");
            }
        }

        // Add new assigned actor
        Actor assignedActor = new Actor(ActorType.ASSIGNED, cmd.assigneeKind(), cmd.assigneeId(), true);
        ticket.getActors().add(assignedActor);

        // Transition status INCOMING → ASSIGNED
        if (ticket.getStatus() == TicketStatus.INCOMING) {
            statusTransitionService.validate(ticket.getStatus(), TicketStatus.ASSIGNED);
            ticket.setStatus(TicketStatus.ASSIGNED);
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
}
