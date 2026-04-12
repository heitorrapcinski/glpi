package com.glpi.ticket.domain.port.in;

import com.glpi.ticket.domain.model.TicketStatus;
import com.glpi.ticket.domain.model.TicketType;

/**
 * Command record for ticket updates.
 * Requirements: 5.8, 5.9
 */
public record UpdateTicketCommand(
        String ticketId,
        String title,
        String content,
        TicketType type,
        TicketStatus status,
        Integer urgency,
        Integer impact,
        Integer priority,
        String categoryId,
        /** Rights bitfield of the requesting user (used for CHANGEPRIORITY check) */
        int userRights
) {}
