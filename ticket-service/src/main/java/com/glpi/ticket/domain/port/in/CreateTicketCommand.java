package com.glpi.ticket.domain.port.in;

import com.glpi.ticket.domain.model.TicketType;

/**
 * Command record for ticket creation.
 * Requirements: 5.3
 */
public record CreateTicketCommand(
        TicketType type,
        String title,
        String content,
        String entityId,
        int urgency,
        int impact,
        String categoryId,
        String requesterId
) {}
