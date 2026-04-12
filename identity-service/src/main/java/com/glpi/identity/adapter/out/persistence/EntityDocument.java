package com.glpi.identity.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document mapping for the Entity aggregate.
 */
@Document(collection = "entities")
@CompoundIndex(name = "name_parentId_unique", def = "{'name': 1, 'parentId': 1}", unique = true)
public class EntityDocument {

    @Id
    private String id;

    private String name;

    @Indexed
    private String parentId;

    private int level;
    private String completeName;
    private EntityConfigDocument config;
    private Instant createdAt;
    private Instant updatedAt;

    public EntityDocument() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getCompleteName() { return completeName; }
    public void setCompleteName(String completeName) { this.completeName = completeName; }

    public EntityConfigDocument getConfig() { return config; }
    public void setConfig(EntityConfigDocument config) { this.config = config; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Embedded config sub-document.
     */
    public static class EntityConfigDocument {
        private int defaultTicketType = -2;
        private int autoAssignMode = -2;
        private int autoCloseDelay = -2;
        private int calendarId = -2;
        private int satisfactionSurveyEnabled = -2;
        private String notificationSenderEmail;

        public EntityConfigDocument() {}

        public int getDefaultTicketType() { return defaultTicketType; }
        public void setDefaultTicketType(int defaultTicketType) { this.defaultTicketType = defaultTicketType; }

        public int getAutoAssignMode() { return autoAssignMode; }
        public void setAutoAssignMode(int autoAssignMode) { this.autoAssignMode = autoAssignMode; }

        public int getAutoCloseDelay() { return autoCloseDelay; }
        public void setAutoCloseDelay(int autoCloseDelay) { this.autoCloseDelay = autoCloseDelay; }

        public int getCalendarId() { return calendarId; }
        public void setCalendarId(int calendarId) { this.calendarId = calendarId; }

        public int getSatisfactionSurveyEnabled() { return satisfactionSurveyEnabled; }
        public void setSatisfactionSurveyEnabled(int satisfactionSurveyEnabled) {
            this.satisfactionSurveyEnabled = satisfactionSurveyEnabled;
        }

        public String getNotificationSenderEmail() { return notificationSenderEmail; }
        public void setNotificationSenderEmail(String notificationSenderEmail) {
            this.notificationSenderEmail = notificationSenderEmail;
        }
    }
}
