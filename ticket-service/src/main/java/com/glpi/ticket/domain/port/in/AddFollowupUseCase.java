package com.glpi.ticket.domain.port.in;

import com.glpi.ticket.domain.model.Ticket;

/**
 * Driving port — add a followup to a ticket.
 * Requirements: 7.1, 5.7, 7.6
 */
public interface AddFollowupUseCase {
    Ticket addFollowup(AddFollowupCommand cmd);
}
