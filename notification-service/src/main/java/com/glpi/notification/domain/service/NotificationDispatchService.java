package com.glpi.notification.domain.service;

import com.glpi.notification.domain.model.*;
import com.glpi.notification.domain.port.in.NotificationDispatchUseCase;
import com.glpi.notification.domain.port.out.DeliveryPort;
import com.glpi.notification.domain.port.out.NotificationTemplateRepository;
import com.glpi.notification.domain.port.out.QueuedNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates notification dispatch: resolve template, render, create QueuedNotification, deliver.
 * Requirements: 16.1, 16.5, 16.7, 16.8
 */
@Service
public class NotificationDispatchService implements NotificationDispatchUseCase {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final NotificationTargetResolver targetResolver;
    private final NotificationTemplateRepository templateRepository;
    private final QueuedNotificationRepository queuedNotificationRepository;
    private final DeliveryPort emailDeliveryAdapter;
    private final DeliveryPort webhookDeliveryAdapter;

    public NotificationDispatchService(NotificationTargetResolver targetResolver,
                                       NotificationTemplateRepository templateRepository,
                                       QueuedNotificationRepository queuedNotificationRepository,
                                       @Qualifier("emailDeliveryAdapter") DeliveryPort emailDeliveryAdapter,
                                       @Qualifier("webhookDeliveryAdapter") DeliveryPort webhookDeliveryAdapter) {
        this.targetResolver = targetResolver;
        this.templateRepository = templateRepository;
        this.queuedNotificationRepository = queuedNotificationRepository;
        this.emailDeliveryAdapter = emailDeliveryAdapter;
        this.webhookDeliveryAdapter = webhookDeliveryAdapter;
    }

    @Override
    public void dispatch(String eventType, List<Actor> actors, Map<String, Object> context) {
        // Resolve template
        var templateOpt = templateRepository.findByEventName(eventType);
        if (templateOpt.isEmpty()) {
            log.warn("No notification template found for event type: {}", eventType);
            return;
        }
        NotificationTemplate template = templateOpt.get();

        // Resolve targets
        List<NotificationTarget> targets = targetResolver.resolve(actors, context);
        if (targets.isEmpty()) {
            log.info("No notification targets resolved for event type: {}", eventType);
            return;
        }

        // Render and dispatch for each target
        for (NotificationTarget target : targets) {
            String subject = renderTemplate(template.getSubjectTemplate(), context);
            String body = renderTemplate(template.getBodyTemplate(), context);

            QueuedNotification notification = new QueuedNotification();
            notification.setId(UUID.randomUUID().toString());
            notification.setEventType(eventType);
            notification.setChannel(target.channel());
            notification.setRecipientId(target.recipientId());
            notification.setRecipientAddress(target.recipientAddress());
            notification.setSubject(subject);
            notification.setBody(body);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setAttempts(0);
            notification.setCreatedAt(Instant.now());

            QueuedNotification saved = queuedNotificationRepository.save(notification);

            DeliveryPort adapter = (target.channel() == NotificationChannel.WEBHOOK)
                    ? webhookDeliveryAdapter : emailDeliveryAdapter;

            try {
                adapter.deliver(saved);
                saved.setStatus(NotificationStatus.SENT);
                saved.setDeliveredAt(Instant.now());
                log.info("Notification delivered to {} for event {}", target.recipientAddress(), eventType);
            } catch (NotificationDeliveryException e) {
                saved.setStatus(NotificationStatus.FAILED);
                saved.setErrorMessage(e.getMessage());
                saved.setLastAttemptAt(Instant.now());
                log.error("Notification delivery failed for {} on event {}: {}",
                        target.recipientAddress(), eventType, e.getMessage());
            }
            saved.setAttempts(saved.getAttempts() + 1);
            queuedNotificationRepository.save(saved);
        }
    }

    /**
     * Simple template rendering — replaces {{key}} placeholders with context values.
     */
    private String renderTemplate(String template, Map<String, Object> context) {
        if (template == null || context == null) {
            return template;
        }
        String rendered = template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}",
                    entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return rendered;
    }
}
