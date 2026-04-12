package com.glpi.notification.domain.model;

/**
 * Thrown when a notification template is not found.
 */
public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(String id) {
        super("Notification template not found: " + id);
    }
}
