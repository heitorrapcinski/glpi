package com.glpi.ticket.domain.port.in;

/**
 * Command record for requesting a validation on a ticket.
 * Requirements: 8.1, 8.2
 */
public record RequestValidationCommand(
        String ticketId,
        String validatorId,
        String validatorKind
) {}
