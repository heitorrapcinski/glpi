package com.glpi.notification.domain.port.in;

/**
 * Command for creating a notification template.
 */
public record CreateTemplateCommand(
        String eventName,
        String subjectTemplate,
        String bodyTemplate,
        String language
) {}
