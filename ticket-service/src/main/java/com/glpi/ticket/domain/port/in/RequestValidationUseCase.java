package com.glpi.ticket.domain.port.in;

import com.glpi.ticket.domain.model.Ticket;

/**
 * Driving port — request a validation on a ticket.
 * Requirements: 8.1, 8.2
 */
public interface RequestValidationUseCase {
    Ticket requestValidation(RequestValidationCommand cmd);
}
