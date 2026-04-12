package com.glpi.ticket.domain.service;

import com.glpi.ticket.domain.model.InvalidStatusTransitionException;
import com.glpi.ticket.domain.model.TicketStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StatusTransitionService.
 * Requirements: 26.1, 26.5, 26.6
 */
class StatusTransitionServiceTest {

    private final StatusTransitionService service = new StatusTransitionService();

    @Test
    void validate_incomingToAssigned_allowed() {
        assertDoesNotThrow(() -> service.validate(TicketStatus.INCOMING, TicketStatus.ASSIGNED));
    }

    @Test
    void validate_solvedToClosed_allowed() {
        assertDoesNotThrow(() -> service.validate(TicketStatus.SOLVED, TicketStatus.CLOSED));
    }

    @Test
    void validate_sameStatus_allowed() {
        assertDoesNotThrow(() -> service.validate(TicketStatus.ASSIGNED, TicketStatus.ASSIGNED));
    }

    @Test
    void validate_closedToAssigned_throwsInvalidTransition() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> service.validate(TicketStatus.CLOSED, TicketStatus.ASSIGNED));
    }
}
