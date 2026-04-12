package com.glpi.notification.domain.model;

/**
 * Resolved notification target — a recipient to be notified.
 * Requirements: 16.5
 */
public record NotificationTarget(
        String recipientId,
        String recipientAddress,
        NotificationChannel channel
) {}
