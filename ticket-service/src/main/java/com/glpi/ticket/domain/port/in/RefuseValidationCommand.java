package com.glpi.ticket.domain.port.in;

/**
 * Command record for refusing a ticket validation.
 * Requirements: 8.4
 */
public record RefuseValidationCommand(
        String ticketId,
        String validationId,
        String comment
) {}
