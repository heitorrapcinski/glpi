package com.glpi.ticket.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.*;
import com.glpi.ticket.domain.port.in.AddSolutionCommand;
import com.glpi.ticket.domain.port.in.AddSolutionUseCase;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing AddSolutionUseCase.
 * Sets solution, transitions ticket to SOLVED, records solvedAt.
 * Requirements: 7.3, 7.4, 5.5
 */
@Service
public class AddSolutionService implements AddSolutionUseCase {

    private final TicketRepository ticketRepository;
    private final EventPublisherPort eventPublisher;

    public AddSolutionService(TicketRepository ticketRepository,
                              EventPublisherPort eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Ticket addSolution(AddSolutionCommand cmd) {
        Ticket ticket = ticketRepository.findById(cmd.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(cmd.ticketId()));

        Instant now = Instant.now();

        Solution solution = new Solution(
                cmd.content(),
                cmd.solutionType(),
                cmd.authorId(),
                now
        );
        ticket.setSolution(solution);
        ticket.setStatus(TicketStatus.SOLVED);
        ticket.setSolvedAt(now);

        // Record solve delay stat (seconds since creation)
        if (ticket.getCreatedAt() != null) {
            long solveDelay = now.getEpochSecond() - ticket.getCreatedAt().getEpochSecond();
            ticket.setSolveDelayStat(solveDelay);
        }

        ticket.setUpdatedAt(now);

        Ticket saved = ticketRepository.save(ticket);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "TicketSolved",
                saved.getId(),
                "Ticket",
                now,
                1,
                saved
        ));

        return saved;
    }
}
