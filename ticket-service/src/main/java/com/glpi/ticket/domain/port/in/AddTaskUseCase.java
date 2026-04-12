package com.glpi.ticket.domain.port.in;

import com.glpi.ticket.domain.model.Ticket;

/**
 * Driving port — add a task to a ticket.
 * Requirements: 7.2, 7.7
 */
public interface AddTaskUseCase {
    Ticket addTask(AddTaskCommand cmd);
}
