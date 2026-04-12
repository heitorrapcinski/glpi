package com.glpi.ticket.domain.port.in;

import com.glpi.ticket.domain.model.Ticket;

/**
 * Driving port — update an existing ticket.
 * Requirements: 5.8, 5.9, 5.10
 */
public interface UpdateTicketUseCase {

    Ticket updateTicket(UpdateTicketCommand command);
}
