package com.glpi.ticket.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.*;
import com.glpi.ticket.domain.port.in.CreateTicketCommand;
import com.glpi.ticket.domain.port.in.CreateTicketUseCase;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing CreateTicketUseCase.
 * Requirements: 5.3, 5.10, 21.2
 */
@Service
public class CreateTicketService implements CreateTicketUseCase {

    private final TicketRepository ticketRepository;
    private final EventPublisherPort eventPublisher;
    private final PriorityMatrixService priorityMatrixService;

    public CreateTicketService(TicketRepository ticketRepository,
                               EventPublisherPort eventPublisher,
                               PriorityMatrixService priorityMatrixService) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
        this.priorityMatrixService = priorityMatrixService;
    }

    @Override
    public Ticket createTicket(CreateTicketCommand cmd) {
        Instant now = Instant.now();

        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID().toString());
        ticket.setType(cmd.type());
        ticket.setStatus(TicketStatus.INCOMING);
        ticket.setTitle(cmd.title());
        ticket.setContent(cmd.content());
        ticket.setEntityId(cmd.entityId());
        ticket.setUrgency(cmd.urgency());
        ticket.setImpact(cmd.impact());
        ticket.setCategoryId(cmd.categoryId());
        ticket.setDeleted(false);
        ticket.setPriorityManualOverride(false);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        // Compute initial priority from matrix
        int priority = priorityMatrixService.computePriority(cmd.urgency(), cmd.impact(), cmd.entityId());
        ticket.setPriority(priority);

        // Add requester actor
        Actor requester = new Actor(ActorType.REQUESTER, "user", cmd.requesterId(), true);
        ticket.getActors().add(requester);

        Ticket saved = ticketRepository.save(ticket);

        // Publish TicketCreated event
        DomainEventEnvelope event = new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "TicketCreated",
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
