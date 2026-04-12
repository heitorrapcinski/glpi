package com.glpi.ticket.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.*;
import com.glpi.ticket.domain.port.in.RefuseValidationCommand;
import com.glpi.ticket.domain.port.in.RefuseValidationUseCase;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing RefuseValidationUseCase.
 * Sets validation status=REFUSED, reopens ticket to INCOMING.
 * Requirements: 8.4
 */
@Service
public class RefuseValidationService implements RefuseValidationUseCase {

    /** Validation status: REFUSED=3 */
    private static final int VALIDATION_REFUSED = 3;

    private final TicketRepository ticketRepository;
    private final EventPublisherPort eventPublisher;

    public RefuseValidationService(TicketRepository ticketRepository,
                                   EventPublisherPort eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Ticket refuseValidation(RefuseValidationCommand cmd) {
        Ticket ticket = ticketRepository.findById(cmd.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(cmd.ticketId()));

        Validation validation = ticket.getValidations().stream()
                .filter(v -> v.getId().equals(cmd.validationId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Validation not found: " + cmd.validationId()));

        validation.setStatus(VALIDATION_REFUSED);
        if (cmd.comment() != null) {
            validation.setComment(cmd.comment());
        }

        Instant now = Instant.now();
        ticket.setStatus(TicketStatus.INCOMING);
        ticket.setUpdatedAt(now);

        Ticket saved = ticketRepository.save(ticket);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "TicketValidationRefused",
                saved.getId(),
                "Ticket",
                now,
                1,
                validation
        ));

        return saved;
    }
}
