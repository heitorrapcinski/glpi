package com.glpi.change.domain.port.in;

/**
 * Command to link a ticket to a change.
 * Requirements: 11.6, 11.7
 */
public record LinkTicketToChangeCommand(
        String changeId,
        String ticketId
) {}
