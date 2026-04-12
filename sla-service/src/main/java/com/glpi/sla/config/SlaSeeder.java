package com.glpi.sla.config;

import com.glpi.sla.domain.model.Calendar;
import com.glpi.sla.domain.model.CalendarSegment;
import com.glpi.sla.domain.port.out.CalendarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds default calendar data on startup when the calendars collection is empty.
 * Default calendar: Mon–Fri 08:00–20:00.
 * Requirements: 29.5, 29.12
 */
@Component
public class SlaSeeder {

    private static final Logger log = LoggerFactory.getLogger(SlaSeeder.class);

    private final CalendarRepository calendarRepository;

    public SlaSeeder(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (!calendarRepository.findAll().isEmpty()) {
            log.info("[Seeder] calendars: collection already has data, skipping");
            return;
        }

        List<CalendarSegment> segments = new ArrayList<>();
        // Monday (1) through Friday (5), 08:00–20:00
        for (int dow = 1; dow <= 5; dow++) {
            segments.add(new CalendarSegment(dow, "08:00", "20:00"));
        }

        Calendar defaultCalendar = new Calendar(
                "1",
                "Default",
                "0",
                true,
                segments,
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );

        calendarRepository.save(defaultCalendar);
        log.info("[Seeder] calendars: inserted 1 document (Default calendar) at {}", Instant.now());
    }
}
