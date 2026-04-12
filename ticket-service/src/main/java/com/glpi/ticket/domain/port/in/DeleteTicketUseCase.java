package com.glpi.ticket.domain.port.in;

/**
 * Driving port — soft-delete a ticket.
 * Requirements: 5.11, 5.12
 */
public interface DeleteTicketUseCase {

    void deleteTicket(String ticketId);
}
