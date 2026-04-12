package com.glpi.sla.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * MongoDB document for the Sla aggregate.
 * Collection: slas
 * Indexes: entityId, type
 */
@Document(collection = "slas")
public class SlaDocument {

    @Id
    private String id;
    private String name;

    @Indexed
    private String entityId;

    @Indexed
    private int type;

    private long durationSeconds;
    private String calendarId;
    private List<LevelDoc> levels;
    private Instant createdAt;
    private Instant updatedAt;

    public SlaDocument() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getCalendarId() { return calendarId; }
    public void setCalendarId(String calendarId) { this.calendarId = calendarId; }

    public List<LevelDoc> getLevels() { return levels; }
    public void setLevels(List<LevelDoc> levels) { this.levels = levels; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    /** Embedded escalation level sub-document. */
    public static class LevelDoc {
        private String id;
        private String name;
        private long executionDelaySeconds;
        private List<Map<String, Object>> actions;

        public LevelDoc() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public long getExecutionDelaySeconds() { return executionDelaySeconds; }
        public void setExecutionDelaySeconds(long v) { this.executionDelaySeconds = v; }

        public List<Map<String, Object>> getActions() { return actions; }
        public void setActions(List<Map<String, Object>> actions) { this.actions = actions; }
    }
}
