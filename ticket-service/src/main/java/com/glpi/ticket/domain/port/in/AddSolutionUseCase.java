package com.glpi.ticket.domain.port.in;

import com.glpi.ticket.domain.model.Ticket;

/**
 * Driving port — add a solution to a ticket (transitions to SOLVED).
 * Requirements: 7.3, 7.4, 5.5
 */
public interface AddSolutionUseCase {
    Ticket addSolution(AddSolutionCommand cmd);
}
