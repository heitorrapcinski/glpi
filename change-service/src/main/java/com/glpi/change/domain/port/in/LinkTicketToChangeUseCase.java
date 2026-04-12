package com.glpi.change.domain.port.in;

import com.glpi.change.domain.model.Change;

/**
 * Driving port — link a ticket to a change.
 * Requirements: 11.6, 11.7
 */
public interface LinkTicketToChangeUseCase {

    Change linkTicket(LinkTicketToChangeCommand cmd);
}
