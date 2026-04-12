package com.glpi.ticket.domain.port.in;

import com.glpi.ticket.domain.model.Ticket;

/**
 * Driving port — create a new ticket.
 * Requirements: 5.3, 5.10
 */
public interface CreateTicketUseCase {

    Ticket createTicket(CreateTicketCommand command);
}
