package com.glpi.ticket.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.ticket.domain.model.*;
import com.glpi.ticket.domain.port.in.ApproveValidationCommand;
import com.glpi.ticket.domain.port.in.ApproveValidationUseCase;
import com.glpi.ticket.domain.port.out.EventPublisherPort;
import com.glpi.ticket.domain.port.out.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing ApproveValidationUseCase.
 * Sets validation status=ACCEPTED. If all required validations accepted,
 * allows SOLVED transition (global mode: any one approves).
 * Requirements: 8.3, 8.5
 */
@Service
public class ApproveValidationService implements ApproveValidationUseCase {

    /** Validation status: WAITING=1, ACCEPTED=2 */
    private static final int VALIDATION_WAITING = 1;
    private static final int VALIDATION_ACCEPTED = 2;

    private final TicketRepository ticketRepository;
    private final EventPublisherPort eventPublisher;

    public ApproveValidationService(TicketRepository ticketRepository,
                                    EventPublisherPort eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Ticket approveValidation(ApproveValidationCommand cmd) {
        Ticket ticket = ticketRepository.findById(cmd.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(cmd.ticketId()));

        Validation validation = ticket.getValidations().stream()
                .filter(v -> v.getId().equals(cmd.validationId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Validation not found: " + cmd.validationId()));

        validation.setStatus(VALIDATION_ACCEPTED);
        if (cmd.comment() != null) {
            validation.setComment(cmd.comment());
        }

        Instant now = Instant.now();
        ticket.setUpdatedAt(now);

        // If all validations are accepted (or at least one in global mode),
        // allow ticket to proceed — revert from WAITING to ASSIGNED
        boolean allAccepted = ticket.getValidations().stream()
                .noneMatch(v -> v.getStatus() == VALIDATION_WAITING);
        if (allAccepted && ticket.getStatus() == TicketStatus.WAITING) {
            ticket.setStatus(TicketStatus.ASSIGNED);
        }

        Ticket saved = ticketRepository.save(ticket);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "TicketValidationApproved",
                saved.getId(),
                "Ticket",
                now,
                1,
                validation
        ));

        return saved;
    }
}
