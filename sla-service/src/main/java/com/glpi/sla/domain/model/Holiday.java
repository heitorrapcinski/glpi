package com.glpi.sla.domain.model;

import java.time.LocalDate;

/**
 * Value object representing a holiday linked to a calendar.
 * When isRecurring=true the holiday repeats every year on the same month/day.
 * Requirements: 14.6
 */
public record Holiday(
        String id,
        String name,
        LocalDate date,
        boolean isRecurring
) {}
