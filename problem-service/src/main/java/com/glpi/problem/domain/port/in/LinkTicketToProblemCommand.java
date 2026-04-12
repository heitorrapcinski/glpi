package com.glpi.problem.domain.port.in;

/**
 * Command to link a ticket to a problem.
 * Requirements: 10.3, 10.4
 */
public record LinkTicketToProblemCommand(String problemId, String ticketId) {}
