package com.glpi.sla.domain.service;

import com.glpi.sla.domain.model.Calendar;
import com.glpi.sla.domain.model.CalendarNotFoundException;
import com.glpi.sla.domain.model.CalendarSegment;
import com.glpi.sla.domain.model.Holiday;
import com.glpi.sla.domain.port.in.DeadlineComputationPort;
import com.glpi.sla.domain.port.out.CalendarRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Domain service implementing business-hours deadline computation.
 * Advances time minute-by-minute through business segments, skipping non-business
 * hours and holidays defined in the associated calendar.
 * Requirements: 14.7, 14.8
 */
@Service
public class DeadlineComputationService implements DeadlineComputationPort {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    // Step size in seconds for iteration (60s = 1 minute for efficiency)
    private static final long STEP_SECONDS = 60L;

    private final CalendarRepository calendarRepository;

    public DeadlineComputationService(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @Override
    public Instant computeDeadline(Instant start, long durationSeconds, String calendarId) {
        Calendar calendar = loadCalendar(calendarId);

        long remaining = durationSeconds;
        ZonedDateTime current = start.atZone(ZoneOffset.UTC);

        while (remaining > 0) {
            if (isBusinessSecond(current, calendar)) {
                remaining -= STEP_SECONDS;
                if (remaining < 0) {
                    // Overshoot: back up by the overshoot amount
                    current = current.plusSeconds(remaining);
                    remaining = 0;
                    break;
                }
            }
            current = current.plusSeconds(STEP_SECONDS);
        }

        return current.toInstant();
    }

    @Override
    public long computeElapsedBusinessSeconds(Instant start, Instant end, String calendarId) {
        if (!end.isAfter(start)) {
            return 0L;
        }
        Calendar calendar = loadCalendar(calendarId);

        long elapsed = 0L;
        ZonedDateTime current = start.atZone(ZoneOffset.UTC);
        ZonedDateTime endDt = end.atZone(ZoneOffset.UTC);

        while (current.isBefore(endDt)) {
            if (isBusinessSecond(current, calendar)) {
                elapsed += STEP_SECONDS;
            }
            current = current.plusSeconds(STEP_SECONDS);
        }

        return elapsed;
    }

    // --- helpers ---

    private Calendar loadCalendar(String calendarId) {
        return calendarRepository.findById(calendarId)
                .orElseThrow(() -> new CalendarNotFoundException(calendarId));
    }

    /**
     * Returns true if the given instant falls within a business segment
     * and is NOT on a holiday.
     */
    private boolean isBusinessSecond(ZonedDateTime dt, Calendar calendar) {
        LocalDate date = dt.toLocalDate();
        LocalTime time = dt.toLocalTime();
        int isoDow = dt.getDayOfWeek().getValue(); // 1=Monday..7=Sunday

        if (isHoliday(date, calendar.getHolidays())) {
            return false;
        }

        return isInBusinessSegment(isoDow, time, calendar.getSegments());
    }

    private boolean isHoliday(LocalDate date, List<Holiday> holidays) {
        for (Holiday h : holidays) {
            if (h.isRecurring()) {
                // Match month and day regardless of year
                if (h.date().getMonthValue() == date.getMonthValue()
                        && h.date().getDayOfMonth() == date.getDayOfMonth()) {
                    return true;
                }
            } else {
                if (h.date().equals(date)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInBusinessSegment(int isoDow, LocalTime time, List<CalendarSegment> segments) {
        for (CalendarSegment seg : segments) {
            if (seg.dayOfWeek() == isoDow) {
                LocalTime start = LocalTime.parse(seg.startTime(), TIME_FMT);
                LocalTime end = LocalTime.parse(seg.endTime(), TIME_FMT);
                if (!time.isBefore(start) && time.isBefore(end)) {
                    return true;
                }
            }
        }
        return false;
    }
}
