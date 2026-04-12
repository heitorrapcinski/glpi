package com.glpi.ticket.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.*;
import com.glpi.ticket.domain.port.in.AddFollowupCommand;
import com.glpi.ticket.domain.port.in.AddFollowupUseCase;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing AddFollowupUseCase.
 * If ticket is CLOSED/SOLVED and author is requester: reopen to INCOMING.
 * Always publishes TicketFollowupAdded; publishes TicketReopened when reopened.
 * Requirements: 7.1, 7.6, 5.7, 26.2
 */
@Service
public class AddFollowupService implements AddFollowupUseCase {

    private final TicketRepository ticketRepository;
    private final EventPublisherPort eventPublisher;

    public AddFollowupService(TicketRepository ticketRepository,
                              EventPublisherPort eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Ticket addFollowup(AddFollowupCommand cmd) {
        Ticket ticket = ticketRepository.findById(cmd.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(cmd.ticketId()));

        Instant now = Instant.now();

        // Check if author is a requester on this ticket
        boolean isRequester = ticket.getActors().stream()
                .anyMatch(a -> a.getActorType() == ActorType.REQUESTER
                        && a.getActorId().equals(cmd.authorId()));

        boolean wasReopened = false;
        if (isRequester
                && (ticket.getStatus() == TicketStatus.CLOSED
                    || ticket.getStatus() == TicketStatus.SOLVED)) {
            ticket.setStatus(TicketStatus.INCOMING);
            wasReopened = true;
        }

        Followup followup = new Followup(
                UUID.randomUUID().toString(),
                cmd.content(),
                cmd.authorId(),
                cmd.isPrivate(),
                cmd.source(),
                now
        );
        ticket.getFollowups().add(followup);
        ticket.setUpdatedAt(now);

        Ticket saved = ticketRepository.save(ticket);

        if (wasReopened) {
            eventPublisher.publish(new DomainEventEnvelope(
                    UUID.randomUUID().toString(),
                    "TicketReopened",
                    saved.getId(),
                    "Ticket",
                    now,
                    1,
                    saved
            ));
        }

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "TicketFollowupAdded",
                saved.getId(),
                "Ticket",
                now,
                1,
                followup
        ));

        return saved;
    }
}
