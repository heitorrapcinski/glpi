package com.glpi.sla.domain.port.in;

import java.time.Instant;

/**
 * Driving port for SLA deadline computation using business-hours calendars.
 * Requirements: 14.7, 14.8
 */
public interface DeadlineComputationPort {

    /**
     * Computes the deadline instant by advancing {@code durationSeconds} of business time
     * from {@code start}, skipping non-business hours and holidays defined in the calendar.
     *
     * @param start           the starting instant (e.g. ticket creation time)
     * @param durationSeconds the SLA duration in business seconds
     * @param calendarId      the calendar to use for business-hours resolution
     * @return the deadline instant
     */
    Instant computeDeadline(Instant start, long durationSeconds, String calendarId);

    /**
     * Counts only the business seconds between {@code start} and {@code end}
     * according to the given calendar.
     *
     * @param start      the start instant
     * @param end        the end instant
     * @param calendarId the calendar to use
     * @return elapsed business seconds
     */
    long computeElapsedBusinessSeconds(Instant start, Instant end, String calendarId);
}
