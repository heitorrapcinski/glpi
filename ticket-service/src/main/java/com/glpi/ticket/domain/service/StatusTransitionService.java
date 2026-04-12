package com.glpi.ticket.domain.service;

import com.glpi.ticket.domain.model.InvalidStatusTransitionException;
import com.glpi.ticket.domain.model.TicketStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Enforces allowed ticket status transitions per ITIL lifecycle rules.
 * Requirements: 6.5, 6.6, 26.1, 26.5, 26.6
 */
@Service
public class StatusTransitionService {

    /**
     * Default allowed transitions: from → list of allowed targets.
     * Profiles may override this via ticketStatusMatrix.
     */
    private static final Map<TicketStatus, List<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.INCOMING,  List.of(TicketStatus.ASSIGNED, TicketStatus.PLANNED, TicketStatus.WAITING, TicketStatus.SOLVED, TicketStatus.CLOSED),
            TicketStatus.ASSIGNED,  List.of(TicketStatus.PLANNED, TicketStatus.WAITING, TicketStatus.SOLVED, TicketStatus.CLOSED, TicketStatus.INCOMING),
            TicketStatus.PLANNED,   List.of(TicketStatus.ASSIGNED, TicketStatus.WAITING, TicketStatus.SOLVED, TicketStatus.CLOSED),
            TicketStatus.WAITING,   List.of(TicketStatus.ASSIGNED, TicketStatus.PLANNED, TicketStatus.SOLVED, TicketStatus.CLOSED, TicketStatus.INCOMING),
            TicketStatus.SOLVED,    List.of(TicketStatus.CLOSED, TicketStatus.INCOMING, TicketStatus.ASSIGNED),
            TicketStatus.CLOSED,    List.of(TicketStatus.INCOMING)
    );

    /**
     * Validate that the transition from → to is permitted.
     * Throws InvalidStatusTransitionException (HTTP 422) if not allowed.
     */
    public void validate(TicketStatus from, TicketStatus to) {
        if (from == to) return;
        List<TicketStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, List.of());
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }
}
