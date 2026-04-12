package com.glpi.ticket.domain.port.in;

import com.glpi.ticket.domain.model.Ticket;

/**
 * Driving port — reject a ticket solution (reopens to ASSIGNED).
 * Requirements: 7.5
 */
public interface RejectSolutionUseCase {
    Ticket rejectSolution(RejectSolutionCommand cmd);
}
