package com.glpi.ticket.domain.port.in;

/**
 * Command record for rejecting a ticket solution.
 * Requirements: 7.5
 */
public record RejectSolutionCommand(
        String ticketId,
        String rejectorId,
        String reason
) {}
