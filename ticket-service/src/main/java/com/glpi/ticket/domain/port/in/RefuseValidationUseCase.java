package com.glpi.ticket.domain.port.in;

import com.glpi.ticket.domain.model.Ticket;

/**
 * Driving port — refuse a ticket validation (reopens ticket).
 * Requirements: 8.4
 */
public interface RefuseValidationUseCase {
    Ticket refuseValidation(RefuseValidationCommand cmd);
}
