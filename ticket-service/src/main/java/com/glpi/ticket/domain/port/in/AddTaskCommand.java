package com.glpi.ticket.domain.port.in;

import java.time.Instant;

/**
 * Command record for adding a task to a ticket.
 * Requirements: 7.2, 7.7
 */
public record AddTaskCommand(
        String ticketId,
        String content,
        String assignedUserId,
        Instant plannedStart,
        Instant plannedEnd,
        long duration,
        /** 1=TODO, 2=DONE */
        int status,
        boolean isPrivate
) {}
