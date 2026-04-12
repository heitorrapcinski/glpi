package com.glpi.sla.domain.model;

/**
 * Value object representing a business-hours segment within a calendar.
 * dayOfWeek: 1=Monday, 2=Tuesday, ..., 7=Sunday (ISO-8601 convention).
 * startTime / endTime: "HH:mm" format (24-hour clock).
 * Requirements: 14.5
 */
public record CalendarSegment(
        int dayOfWeek,
        String startTime,
        String endTime
) {}
