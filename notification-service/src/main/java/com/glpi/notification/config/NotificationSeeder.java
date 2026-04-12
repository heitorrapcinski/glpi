package com.glpi.notification.config;

import com.glpi.notification.adapter.out.persistence.MongoNotificationTemplateRepository;
import com.glpi.notification.adapter.out.persistence.NotificationTemplateDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Seeds default notification templates on first startup.
 * Requirements: 29.10, 29.12
 */
@Component
public class NotificationSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(NotificationSeeder.class);

    private final MongoNotificationTemplateRepository templateRepository;

    public NotificationSeeder(MongoNotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public void run(String... args) {
        if (templateRepository.count() > 0) {
            log.info("notification_templates collection already contains data, skipping seeding");
            return;
        }

        List<NotificationTemplateDocument> templates = List.of(
                createTemplate("ticket.created",
                        "Ticket #{{aggregateId}} created: {{title}}",
                        "<h2>New Ticket Created</h2><p>Ticket <b>#{{aggregateId}}</b> has been created.</p><p>Title: {{title}}</p>"),
                createTemplate("ticket.solved",
                        "Ticket #{{aggregateId}} solved: {{title}}",
                        "<h2>Ticket Solved</h2><p>Ticket <b>#{{aggregateId}}</b> has been solved.</p><p>Title: {{title}}</p>"),
                createTemplate("ticket.closed",
                        "Ticket #{{aggregateId}} closed: {{title}}",
                        "<h2>Ticket Closed</h2><p>Ticket <b>#{{aggregateId}}</b> has been closed.</p><p>Title: {{title}}</p>"),
                createTemplate("ticket.validation.requested",
                        "Validation requested for Ticket #{{aggregateId}}",
                        "<h2>Validation Requested</h2><p>Your validation is requested for ticket <b>#{{aggregateId}}</b>.</p><p>Title: {{title}}</p>"),
                createTemplate("problem.created",
                        "Problem #{{aggregateId}} created: {{title}}",
                        "<h2>New Problem Created</h2><p>Problem <b>#{{aggregateId}}</b> has been created.</p><p>Title: {{title}}</p>"),
                createTemplate("problem.solved",
                        "Problem #{{aggregateId}} solved: {{title}}",
                        "<h2>Problem Solved</h2><p>Problem <b>#{{aggregateId}}</b> has been solved.</p><p>Title: {{title}}</p>"),
                createTemplate("change.created",
                        "Change #{{aggregateId}} created: {{title}}",
                        "<h2>New Change Created</h2><p>Change <b>#{{aggregateId}}</b> has been created.</p><p>Title: {{title}}</p>"),
                createTemplate("change.validation.approved",
                        "Change #{{aggregateId}} validation approved",
                        "<h2>Change Validation Approved</h2><p>Change <b>#{{aggregateId}}</b> validation has been approved.</p><p>Title: {{title}}</p>")
        );

        templateRepository.saveAll(templates);
        log.info("Seeded {} default notification templates at {}", templates.size(), Instant.now());
    }

    private NotificationTemplateDocument createTemplate(String eventName, String subject, String body) {
        NotificationTemplateDocument doc = new NotificationTemplateDocument();
        doc.setId(UUID.randomUUID().toString());
        doc.setEventName(eventName);
        doc.setSubjectTemplate(subject);
        doc.setBodyTemplate(body);
        doc.setLanguage("en_US");
        doc.setCreatedAt(Instant.now());
        doc.setUpdatedAt(Instant.now());
        return doc;
    }
}
