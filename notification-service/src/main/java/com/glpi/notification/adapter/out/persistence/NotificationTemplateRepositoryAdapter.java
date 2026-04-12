package com.glpi.notification.adapter.out.persistence;

import com.glpi.notification.domain.model.NotificationTemplate;
import com.glpi.notification.domain.port.out.NotificationTemplateRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing NotificationTemplateRepository driven port.
 * Requirements: 16.4
 */
@Component
public class NotificationTemplateRepositoryAdapter implements NotificationTemplateRepository {

    private final MongoNotificationTemplateRepository mongo;

    public NotificationTemplateRepositoryAdapter(MongoNotificationTemplateRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<NotificationTemplate> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<NotificationTemplate> findByEventName(String eventName) {
        return mongo.findByEventName(eventName).map(this::toDomain);
    }

    @Override
    public NotificationTemplate save(NotificationTemplate template) {
        NotificationTemplateDocument doc = toDocument(template);
        NotificationTemplateDocument saved = mongo.save(doc);
        return toDomain(saved);
    }

    @Override
    public List<NotificationTemplate> findAll(int page, int size) {
        return mongo.findAllBy(PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return mongo.count();
    }

    private NotificationTemplate toDomain(NotificationTemplateDocument doc) {
        NotificationTemplate t = new NotificationTemplate();
        t.setId(doc.getId());
        t.setEventName(doc.getEventName());
        t.setSubjectTemplate(doc.getSubjectTemplate());
        t.setBodyTemplate(doc.getBodyTemplate());
        t.setLanguage(doc.getLanguage());
        t.setCreatedAt(doc.getCreatedAt());
        t.setUpdatedAt(doc.getUpdatedAt());
        return t;
    }

    private NotificationTemplateDocument toDocument(NotificationTemplate t) {
        NotificationTemplateDocument doc = new NotificationTemplateDocument();
        doc.setId(t.getId());
        doc.setEventName(t.getEventName());
        doc.setSubjectTemplate(t.getSubjectTemplate());
        doc.setBodyTemplate(t.getBodyTemplate());
        doc.setLanguage(t.getLanguage());
        doc.setCreatedAt(t.getCreatedAt());
        doc.setUpdatedAt(t.getUpdatedAt());
        return doc;
    }
}
