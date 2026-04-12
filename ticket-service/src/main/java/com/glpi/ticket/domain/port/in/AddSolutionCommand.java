package com.glpi.ticket.domain.port.in;

/**
 * Command record for adding a solution to a ticket.
 * Requirements: 7.3, 7.4, 5.5
 */
public record AddSolutionCommand(
        String ticketId,
        String content,
        String solutionType,
        String authorId
) {}
