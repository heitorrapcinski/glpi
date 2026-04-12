package com.glpi.problem.domain.port.in;

import com.glpi.problem.domain.model.Problem;

/**
 * Driving port — link a ticket to a problem.
 * Requirements: 10.3, 10.4
 */
public interface LinkTicketToProblemUseCase {

    Problem linkTicket(LinkTicketToProblemCommand cmd);
}
