package com.glpi.ticket.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.*;
import com.glpi.ticket.domain.port.in.RequestValidationCommand;
import com.glpi.ticket.domain.port.in.RequestValidationUseCase;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing RequestValidationUseCase.
 * Creates a Validation record (status=WAITING), sets ticket to WAITING.
 * Requirements: 8.1, 8.2
 */
@Service
public class RequestValidationService implements RequestValidationUseCase {

    /** Validation status: WAITING=1 */
    private static final int VALIDATION_WAITING = 1;

    private final TicketRepository ticketRepository;
    private final EventPublisherPort eventPublisher;

    public RequestValidationService(TicketRepository ticketRepository,
                                    EventPublisherPort eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Ticket requestValidation(RequestValidationCommand cmd) {
        Ticket ticket = ticketRepository.findById(cmd.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(cmd.ticketId()));

        Instant now = Instant.now();

        Validation validation = new Validation(
                UUID.randomUUID().toString(),
                cmd.validatorId(),
                cmd.validatorKind(),
                VALIDATION_WAITING,
                null
        );
        ticket.getValidations().add(validation);
        ticket.setStatus(TicketStatus.WAITING);
        ticket.setUpdatedAt(now);

        Ticket saved = ticketRepository.save(ticket);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "TicketValidationRequested",
                saved.getId(),
                "Ticket",
                now,
                1,
                validation
        ));

        return saved;
    }
}
