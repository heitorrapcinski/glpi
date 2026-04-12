package com.glpi.sla.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * SLA aggregate — Service Level Agreement with escalation levels.
 * Requirements: 14.1, 14.2, 14.3, 22.7
 */
public class Sla {

    private String id;
    private String name;
    private String entityId;
    private SlaType type;
    private long durationSeconds;
    private String calendarId;
    private List<SlaLevel> levels;
    private Instant createdAt;
    private Instant updatedAt;

    public Sla() {}

    public Sla(String id, String name, String entityId, SlaType type,
               long durationSeconds, String calendarId, List<SlaLevel> levels,
               Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.entityId = entityId;
        this.type = type;
        this.durationSeconds = durationSeconds;
        this.calendarId = calendarId;
        this.levels = levels != null ? new ArrayList<>(levels) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public SlaType getType() { return type; }
    public void setType(SlaType type) { this.type = type; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getCalendarId() { return calendarId; }
    public void setCalendarId(String calendarId) { this.calendarId = calendarId; }

    public List<SlaLevel> getLevels() { return levels; }
    public void setLevels(List<SlaLevel> levels) { this.levels = levels; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
