package com.glpi.ticket.domain.port.in;

/**
 * Command record for approving a ticket validation.
 * Requirements: 8.3
 */
public record ApproveValidationCommand(
        String ticketId,
        String validationId,
        String comment
) {}
