package com.glpi.sla.application.usecase;

import java.time.Instant;

/**
 * Minimal ticket SLA projection returned by the Ticket Service HTTP client.
 * Contains only the fields needed for escalation evaluation.
 */
public record TicketSlaInfo(
        String ticketId,
        String slaId,
        Instant ttoDeadline,
        Instant ttrDeadline
) {}
