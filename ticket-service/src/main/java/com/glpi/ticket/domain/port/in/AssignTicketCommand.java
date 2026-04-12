package com.glpi.ticket.domain.port.in;

/**
 * Command record for ticket assignment.
 * Requirements: 5.4, 6.5, 6.6
 */
public record AssignTicketCommand(
        String ticketId,
        String assigneeId,
        /** "user" or "group" */
        String assigneeKind,
        /** Rights bitfield of the requesting user */
        int userRights
) {}
