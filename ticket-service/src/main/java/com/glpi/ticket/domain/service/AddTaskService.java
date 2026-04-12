package com.glpi.ticket.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.Ticket;
import com.glpi.ticket.domain.model.TicketNotFoundException;
import com.glpi.ticket.domain.model.TicketTask;
import com.glpi.ticket.domain.port.in.AddTaskCommand;
import com.glpi.ticket.domain.port.in.AddTaskUseCase;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing AddTaskUseCase.
 * Publishes TicketTaskCompleted when task status=DONE.
 * Requirements: 7.2, 7.7
 */
@Service
public class AddTaskService implements AddTaskUseCase {

    private static final int TASK_STATUS_DONE = 2;

    private final TicketRepository ticketRepository;
    private final EventPublisherPort eventPublisher;

    public AddTaskService(TicketRepository ticketRepository,
                          EventPublisherPort eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Ticket addTask(AddTaskCommand cmd) {
        Ticket ticket = ticketRepository.findById(cmd.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(cmd.ticketId()));

        Instant now = Instant.now();

        TicketTask task = new TicketTask(
                UUID.randomUUID().toString(),
                cmd.content(),
                cmd.assignedUserId(),
                cmd.status(),
                cmd.isPrivate(),
                cmd.plannedStart(),
                cmd.plannedEnd(),
                cmd.duration(),
                now
        );
        ticket.getTasks().add(task);
        ticket.setUpdatedAt(now);

        Ticket saved = ticketRepository.save(ticket);

        if (cmd.status() == TASK_STATUS_DONE) {
            eventPublisher.publish(new DomainEventEnvelope(
                    UUID.randomUUID().toString(),
                    "TicketTaskCompleted",
                    saved.getId(),
                    "Ticket",
                    now,
                    1,
                    task
            ));
        }

        return saved;
    }
}
