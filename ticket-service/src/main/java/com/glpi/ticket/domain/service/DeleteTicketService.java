package com.glpi.ticket.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.Ticket;
import com.glpi.ticket.domain.model.TicketNotFoundException;
import com.glpi.ticket.domain.port.in.DeleteTicketUseCase;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing DeleteTicketUseCase.
 * Sets isDeleted=true and publishes TicketDeleted event.
 * Requirements: 5.11, 5.12
 */
@Service
public class DeleteTicketService implements DeleteTicketUseCase {

    private final TicketRepository ticketRepository;
    private final EventPublisherPort eventPublisher;

    public DeleteTicketService(TicketRepository ticketRepository, EventPublisherPort eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void deleteTicket(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        ticket.setDeleted(true);
        ticket.setUpdatedAt(Instant.now());
        ticketRepository.save(ticket);

        DomainEventEnvelope event = new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "TicketDeleted",
                ticketId,
                "Ticket",
                Instant.now(),
                1,
                ticket
        );
        eventPublisher.publish(event);
    }
}
