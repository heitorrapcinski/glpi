package com.glpi.notification.domain.port.in;

/**
 * Command for updating a notification template.
 */
public record UpdateTemplateCommand(
        String subjectTemplate,
        String bodyTemplate,
        String language
) {}
