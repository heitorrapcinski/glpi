package com.glpi.notification.domain.model;

import java.time.Instant;

/**
 * NotificationTemplate aggregate — defines the subject and body templates for a notification event.
 * Requirements: 16.4
 */
public class NotificationTemplate {

    private String id;
    private String eventName;
    private String subjectTemplate;
    private String bodyTemplate;
    private String language;
    private Instant createdAt;
    private Instant updatedAt;

    public NotificationTemplate() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getSubjectTemplate() { return subjectTemplate; }
    public void setSubjectTemplate(String subjectTemplate) { this.subjectTemplate = subjectTemplate; }

    public String getBodyTemplate() { return bodyTemplate; }
    public void setBodyTemplate(String bodyTemplate) { this.bodyTemplate = bodyTemplate; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
