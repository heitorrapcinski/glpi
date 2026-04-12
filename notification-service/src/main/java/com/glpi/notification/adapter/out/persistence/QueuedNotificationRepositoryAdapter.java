package com.glpi.notification.adapter.out.persistence;

import com.glpi.notification.domain.model.QueuedNotification;
import com.glpi.notification.domain.port.out.QueuedNotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing QueuedNotificationRepository driven port.
 * Requirements: 16.7
 */
@Component
public class QueuedNotificationRepositoryAdapter implements QueuedNotificationRepository {

    private final MongoQueuedNotificationRepository mongo;

    public QueuedNotificationRepositoryAdapter(MongoQueuedNotificationRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<QueuedNotification> findById(String id) {
        return mongo.findById(id).map(this::toDomain);
    }

    @Override
    public QueuedNotification save(QueuedNotification notification) {
        QueuedNotificationDocument doc = toDocument(notification);
        QueuedNotificationDocument saved = mongo.save(doc);
        return toDomain(saved);
    }

    @Override
    public List<QueuedNotification> findAll(int page, int size) {
        return mongo.findAllBy(PageRequest.of(page, size))
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return mongo.count();
    }

    private QueuedNotification toDomain(QueuedNotificationDocument doc) {
        QueuedNotification n = new QueuedNotification();
        n.setId(doc.getId());
        n.setEventType(doc.getEventType());
        n.setChannel(doc.getChannel());
        n.setRecipientId(doc.getRecipientId());
        n.setRecipientAddress(doc.getRecipientAddress());
        n.setSubject(doc.getSubject());
        n.setBody(doc.getBody());
        n.setStatus(doc.getStatus());
        n.setAttempts(doc.getAttempts());
        n.setLastAttemptAt(doc.getLastAttemptAt());
        n.setDeliveredAt(doc.getDeliveredAt());
        n.setErrorMessage(doc.getErrorMessage());
        n.setCreatedAt(doc.getCreatedAt());
        return n;
    }

    private QueuedNotificationDocument toDocument(QueuedNotification n) {
        QueuedNotificationDocument doc = new QueuedNotificationDocument();
        doc.setId(n.getId());
        doc.setEventType(n.getEventType());
        doc.setChannel(n.getChannel());
        doc.setRecipientId(n.getRecipientId());
        doc.setRecipientAddress(n.getRecipientAddress());
        doc.setSubject(n.getSubject());
        doc.setBody(n.getBody());
        doc.setStatus(n.getStatus());
        doc.setAttempts(n.getAttempts());
        doc.setLastAttemptAt(n.getLastAttemptAt());
        doc.setDeliveredAt(n.getDeliveredAt());
        doc.setErrorMessage(n.getErrorMessage());
        doc.setCreatedAt(n.getCreatedAt());
        return doc;
    }
}
