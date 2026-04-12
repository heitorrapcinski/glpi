package com.glpi.ticket.domain.port.in;

import com.glpi.ticket.domain.model.Ticket;

/**
 * Driving port — approve a ticket validation.
 * Requirements: 8.3
 */
public interface ApproveValidationUseCase {
    Ticket approveValidation(ApproveValidationCommand cmd);
}
