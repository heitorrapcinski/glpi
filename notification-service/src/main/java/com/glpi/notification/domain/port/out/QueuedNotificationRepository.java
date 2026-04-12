package com.glpi.notification.domain.port.out;

import com.glpi.notification.domain.model.QueuedNotification;

import java.util.List;
import java.util.Optional;

/**
 * Driven port — persistence contract for QueuedNotification aggregate.
 * Requirements: 16.7
 */
public interface QueuedNotificationRepository {

    Optional<QueuedNotification> findById(String id);

    QueuedNotification save(QueuedNotification notification);

    List<QueuedNotification> findAll(int page, int size);

    long countAll();
}
