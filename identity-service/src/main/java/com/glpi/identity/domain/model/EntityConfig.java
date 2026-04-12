package com.glpi.identity.domain.model;

/**
 * Embedded configuration object for an Entity.
 * Fields set to CONFIG_PARENT (-2) are inherited from the parent entity.
 */
public class EntityConfig {

    public static final int CONFIG_PARENT = -2;

    private int defaultTicketType;
    private int autoAssignMode;
    private int autoCloseDelay;
    private int calendarId;
    private int satisfactionSurveyEnabled;
    private String notificationSenderEmail;

    public EntityConfig() {
        this.defaultTicketType = CONFIG_PARENT;
        this.autoAssignMode = CONFIG_PARENT;
        this.autoCloseDelay = CONFIG_PARENT;
        this.calendarId = CONFIG_PARENT;
        this.satisfactionSurveyEnabled = CONFIG_PARENT;
        this.notificationSenderEmail = null;
    }

    public EntityConfig(
            int defaultTicketType,
            int autoAssignMode,
            int autoCloseDelay,
            int calendarId,
            int satisfactionSurveyEnabled,
            String notificationSenderEmail) {
        this.defaultTicketType = defaultTicketType;
        this.autoAssignMode = autoAssignMode;
        this.autoCloseDelay = autoCloseDelay;
        this.calendarId = calendarId;
        this.satisfactionSurveyEnabled = satisfactionSurveyEnabled;
        this.notificationSenderEmail = notificationSenderEmail;
    }

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
