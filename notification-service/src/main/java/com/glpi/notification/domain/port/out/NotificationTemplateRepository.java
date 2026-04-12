package com.glpi.notification.domain.port.out;

import com.glpi.notification.domain.model.NotificationTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Driven port — persistence contract for NotificationTemplate aggregate.
 * Requirements: 16.4
 */
public interface NotificationTemplateRepository {

    Optional<NotificationTemplate> findById(String id);

    Optional<NotificationTemplate> findByEventName(String eventName);

    NotificationTemplate save(NotificationTemplate template);

    List<NotificationTemplate> findAll(int page, int size);

    long countAll();
}
