package com.glpi.sla.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Calendar aggregate — defines business hours and holidays used for SLA/OLA deadline computation.
 * Requirements: 14.5, 14.6
 */
public class Calendar {

    private String id;
    private String name;
    private String entityId;
    private boolean isRecursive;
    private List<CalendarSegment> segments;
    private List<Holiday> holidays;
    private Instant createdAt;
    private Instant updatedAt;

    public Calendar() {}

    public Calendar(String id, String name, String entityId, boolean isRecursive,
                    List<CalendarSegment> segments, List<Holiday> holidays,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.entityId = entityId;
        this.isRecursive = isRecursive;
        this.segments = segments != null ? new ArrayList<>(segments) : new ArrayList<>();
        this.holidays = holidays != null ? new ArrayList<>(holidays) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public boolean isRecursive() { return isRecursive; }
    public void setRecursive(boolean recursive) { isRecursive = recursive; }

    public List<CalendarSegment> getSegments() { return segments; }
    public void setSegments(List<CalendarSegment> segments) { this.segments = segments; }

    public List<Holiday> getHolidays() { return holidays; }
    public void setHolidays(List<Holiday> holidays) { this.holidays = holidays; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
