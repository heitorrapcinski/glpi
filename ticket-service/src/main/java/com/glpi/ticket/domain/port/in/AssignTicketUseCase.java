package com.glpi.ticket.domain.port.in;

import com.glpi.ticket.domain.model.Ticket;

/**
 * Driving port — assign a ticket to a user or group.
 * Requirements: 5.4, 6.5, 6.6
 */
public interface AssignTicketUseCase {

    Ticket assignTicket(AssignTicketCommand command);
}
