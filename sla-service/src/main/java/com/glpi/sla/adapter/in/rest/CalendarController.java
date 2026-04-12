package com.glpi.sla.adapter.in.rest;

import com.glpi.common.PagedResponse;
import com.glpi.sla.domain.model.Calendar;
import com.glpi.sla.domain.model.CalendarNotFoundException;
import com.glpi.sla.domain.model.Holiday;
import com.glpi.sla.domain.port.out.CalendarRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Calendar CRUD and holiday management.
 * Requirements: 14.4, 14.8, 19.1, 19.6
 */
@RestController
@RequestMapping("/calendars")
@Tag(name = "Calendars", description = "Business-hours calendar management")
public class CalendarController {

    private final CalendarRepository calendarRepository;

    public CalendarController(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @GetMapping
    @Operation(summary = "List all calendars (paginated)")
    public PagedResponse<Calendar> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(value = "expand_dropdowns", required = false) Boolean expandDropdowns) {
        int clampedSize = Math.min(Math.max(size, 1), 500);
        List<Calendar> all = calendarRepository.findAll();
        int from = Math.min(page * clampedSize, all.size());
        int to = Math.min(from + clampedSize, all.size());
        return PagedResponse.of(all.subList(from, to), all.size(), page, clampedSize);
    }

    @PostMapping
    @Operation(summary = "Create a calendar")
    public ResponseEntity<Calendar> create(@RequestBody Map<String, Object> body) {
        Calendar cal = fromBody(UUID.randomUUID().toString(), body);
        cal.setCreatedAt(Instant.now());
        cal.setUpdatedAt(Instant.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarRepository.save(cal));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a calendar by ID")
    public Calendar getById(@PathVariable String id) {
        return calendarRepository.findById(id)
                .orElseThrow(() -> new CalendarNotFoundException(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a calendar")
    public Calendar update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        calendarRepository.findById(id).orElseThrow(() -> new CalendarNotFoundException(id));
        Calendar cal = fromBody(id, body);
        cal.setUpdatedAt(Instant.now());
        return calendarRepository.save(cal);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a calendar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        calendarRepository.findById(id).orElseThrow(() -> new CalendarNotFoundException(id));
        calendarRepository.delete(id);
    }

    @PostMapping("/{id}/holidays")
    @Operation(summary = "Add a holiday to a calendar")
    public ResponseEntity<Calendar> addHoliday(@PathVariable String id,
                                               @RequestBody Map<String, Object> body) {
        Calendar cal = calendarRepository.findById(id)
                .orElseThrow(() -> new CalendarNotFoundException(id));

        Holiday holiday = new Holiday(
                UUID.randomUUID().toString(),
                (String) body.get("name"),
                LocalDate.parse((String) body.get("date")),
                Boolean.TRUE.equals(body.get("isRecurring"))
        );

        List<Holiday> holidays = new ArrayList<>(cal.getHolidays());
        holidays.add(holiday);
        cal.setHolidays(holidays);
        cal.setUpdatedAt(Instant.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(calendarRepository.save(cal));
    }

    @DeleteMapping("/{id}/holidays/{holidayId}")
    @Operation(summary = "Remove a holiday from a calendar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeHoliday(@PathVariable String id, @PathVariable String holidayId) {
        Calendar cal = calendarRepository.findById(id)
                .orElseThrow(() -> new CalendarNotFoundException(id));

        List<Holiday> holidays = cal.getHolidays().stream()
                .filter(h -> !h.id().equals(holidayId))
                .toList();
        cal.setHolidays(holidays);
        cal.setUpdatedAt(Instant.now());
        calendarRepository.save(cal);
    }

    @SuppressWarnings("unchecked")
    private Calendar fromBody(String id, Map<String, Object> body) {
        Calendar cal = new Calendar();
        cal.setId(id);
        cal.setName((String) body.get("name"));
        cal.setEntityId((String) body.getOrDefault("entityId", "0"));
        cal.setRecursive(Boolean.TRUE.equals(body.get("isRecursive")));

        List<com.glpi.sla.domain.model.CalendarSegment> segments = new ArrayList<>();
        if (body.get("segments") instanceof List<?> rawSegs) {
            for (Object s : rawSegs) {
                Map<String, Object> sm = (Map<String, Object>) s;
                segments.add(new com.glpi.sla.domain.model.CalendarSegment(
                        ((Number) sm.get("dayOfWeek")).intValue(),
                        (String) sm.get("startTime"),
                        (String) sm.get("endTime")
                ));
            }
        }
        cal.setSegments(segments);
        cal.setHolidays(new ArrayList<>());
        return cal;
    }
}
