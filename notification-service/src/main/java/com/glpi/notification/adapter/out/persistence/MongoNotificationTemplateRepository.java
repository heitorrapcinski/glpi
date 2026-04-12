package com.glpi.notification.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for NotificationTemplateDocument.
 * Requirements: 16.4
 */
public interface MongoNotificationTemplateRepository extends MongoRepository<NotificationTemplateDocument, String> {

    Optional<NotificationTemplateDocument> findByEventName(String eventName);

    List<NotificationTemplateDocument> findAllBy(Pageable pageable);
}
