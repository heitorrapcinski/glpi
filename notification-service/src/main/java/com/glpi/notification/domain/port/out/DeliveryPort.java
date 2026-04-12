package com.glpi.notification.domain.port.out;

import com.glpi.notification.domain.model.QueuedNotification;

/**
 * Driven port — notification delivery contract.
 * Requirements: 16.3
 */
public interface DeliveryPort {

    /**
     * Deliver the notification to the recipient.
     * Throws NotificationDeliveryException on failure.
     */
    void deliver(QueuedNotification notification);
}
