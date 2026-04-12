package com.glpi.ticket.domain.port.in;

/**
 * Command record for adding a followup to a ticket.
 * Requirements: 7.1, 5.7
 */
public record AddFollowupCommand(
        String ticketId,
        String content,
        String authorId,
        boolean isPrivate,
        String source
) {}
