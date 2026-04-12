package com.glpi.sla.adapter.out.persistence;

import com.glpi.sla.domain.model.Calendar;
import com.glpi.sla.domain.model.CalendarSegment;
import com.glpi.sla.domain.model.Holiday;
import com.glpi.sla.domain.port.out.CalendarRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MongoDB adapter implementing CalendarRepository.
 */
@Component
public class CalendarRepositoryAdapter implements CalendarRepository {

    private final MongoCalendarRepository mongo;

    public CalendarRepositoryAdapter(MongoCalendarRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Calendar> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public Calendar save(Calendar calendar) {
        CalendarDocument doc = toDocument(calendar);
        return toDomain(mongo.save(doc));
    }

    @Override
    public void delete(String id) {
        mongo.deleteById(id);
    }

    @Override
    public List<Calendar> findAll() {
        return mongo.findAll().stream().map(this::toDomain).toList();
    }

    // --- mapping ---

    private Calendar toDomain(CalendarDocument doc) {
        List<CalendarSegment> segments = new ArrayList<>();
        if (doc.getSegments() != null) {
            for (Map<String, Object> s : doc.getSegments()) {
                int dow = ((Number) s.get("dayOfWeek")).intValue();
                String start = (String) s.get("startTime");
                String end = (String) s.get("endTime");
                segments.add(new CalendarSegment(dow, start, end));
            }
        }

        List<Holiday> holidays = new ArrayList<>();
        if (doc.getHolidays() != null) {
            for (CalendarDocument.HolidayDoc h : doc.getHolidays()) {
                holidays.add(new Holiday(h.getId(), h.getName(), h.getDate(), h.isRecurring()));
            }
        }

        return new Calendar(doc.getId(), doc.getName(), doc.getEntityId(),
                doc.isRecursive(), segments, holidays, doc.getCreatedAt(), doc.getUpdatedAt());
    }

    private CalendarDocument toDocument(Calendar cal) {
        CalendarDocument doc = new CalendarDocument();
        doc.setId(cal.getId());
        doc.setName(cal.getName());
        doc.setEntityId(cal.getEntityId());
        doc.setRecursive(cal.isRecursive());
        doc.setCreatedAt(cal.getCreatedAt());
        doc.setUpdatedAt(cal.getUpdatedAt());

        List<Map<String, Object>> segDocs = new ArrayList<>();
        if (cal.getSegments() != null) {
            for (CalendarSegment s : cal.getSegments()) {
                Map<String, Object> m = new HashMap<>();
                m.put("dayOfWeek", s.dayOfWeek());
                m.put("startTime", s.startTime());
                m.put("endTime", s.endTime());
                segDocs.add(m);
            }
        }
        doc.setSegments(segDocs);

        List<CalendarDocument.HolidayDoc> holDocs = new ArrayList<>();
        if (cal.getHolidays() != null) {
            for (Holiday h : cal.getHolidays()) {
                CalendarDocument.HolidayDoc hd = new CalendarDocument.HolidayDoc();
                hd.setId(h.id());
                hd.setName(h.name());
                hd.setDate(h.date());
                hd.setRecurring(h.isRecurring());
                holDocs.add(hd);
            }
        }
        doc.setHolidays(holDocs);

        return doc;
    }
}
