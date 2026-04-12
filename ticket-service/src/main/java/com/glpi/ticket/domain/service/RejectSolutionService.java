package com.glpi.ticket.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.*;
import com.glpi.ticket.domain.port.in.RejectSolutionCommand;
import com.glpi.ticket.domain.port.in.RejectSolutionUseCase;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing RejectSolutionUseCase.
 * Reopens ticket to ASSIGNED, records rejection reason as a followup.
 * Requirements: 7.5
 */
@Service
public class RejectSolutionService implements RejectSolutionUseCase {

    private final TicketRepository ticketRepository;
    private final EventPublisherPort eventPublisher;

    public RejectSolutionService(TicketRepository ticketRepository,
                                 EventPublisherPort eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Ticket rejectSolution(RejectSolutionCommand cmd) {
        Ticket ticket = ticketRepository.findById(cmd.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(cmd.ticketId()));

        Instant now = Instant.now();

        // Reopen to ASSIGNED
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setSolution(null);
        ticket.setSolvedAt(null);

        // Record rejection reason as a followup
        if (cmd.reason() != null && !cmd.reason().isBlank()) {
            Followup rejectionFollowup = new Followup(
                    UUID.randomUUID().toString(),
                    "Solution rejected: " + cmd.reason(),
                    cmd.rejectorId(),
                    false,
                    "web",
                    now
            );
            ticket.getFollowups().add(rejectionFollowup);
        }

        ticket.setUpdatedAt(now);

        Ticket saved = ticketRepository.save(ticket);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "TicketUpdated",
                saved.getId(),
                "Ticket",
                now,
                1,
                saved
        ));

        return saved;
    }
}
