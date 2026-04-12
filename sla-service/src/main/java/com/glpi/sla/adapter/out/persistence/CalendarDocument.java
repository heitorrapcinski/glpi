package com.glpi.sla.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * MongoDB document for the Calendar aggregate.
 * Collection: calendars
 * Index: entityId
 */
@Document(collection = "calendars")
public class CalendarDocument {

    @Id
    private String id;
    private String name;

    @Indexed
    private String entityId;

    private boolean isRecursive;
    private List<Map<String, Object>> segments;
    private List<HolidayDoc> holidays;
    private Instant createdAt;
    private Instant updatedAt;

    public CalendarDocument() {}

    // Getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public boolean isRecursive() { return isRecursive; }
    public void setRecursive(boolean recursive) { isRecursive = recursive; }

    public List<Map<String, Object>> getSegments() { return segments; }
    public void setSegments(List<Map<String, Object>> segments) { this.segments = segments; }

    public List<HolidayDoc> getHolidays() { return holidays; }
    public void setHolidays(List<HolidayDoc> holidays) { this.holidays = holidays; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    /** Embedded holiday sub-document. */
    public static class HolidayDoc {
        private String id;
        private String name;
        private LocalDate date;
        private boolean isRecurring;

        public HolidayDoc() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public boolean isRecurring() { return isRecurring; }
        public void setRecurring(boolean recurring) { isRecurring = recurring; }
    }
}
