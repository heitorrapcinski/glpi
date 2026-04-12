package com.glpi.notification.domain.model;

/**
 * Status of a queued notification.
 * Requirements: 16.7
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED
}
