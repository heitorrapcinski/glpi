package com.glpi.notification.domain.port.in;

import com.glpi.notification.domain.model.NotificationTemplate;

/**
 * Driving port — CRUD operations for notification templates.
 * Requirements: 16.4
 */
public interface ManageTemplateUseCase {

    NotificationTemplate createTemplate(CreateTemplateCommand command);

    NotificationTemplate updateTemplate(String id, UpdateTemplateCommand command);
}
