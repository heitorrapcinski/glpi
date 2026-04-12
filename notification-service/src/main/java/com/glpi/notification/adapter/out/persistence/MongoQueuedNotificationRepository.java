package com.glpi.notification.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for QueuedNotificationDocument.
 * Requirements: 16.7
 */
public interface MongoQueuedNotificationRepository extends MongoRepository<QueuedNotificationDocument, String> {

    List<QueuedNotificationDocument> findAllBy(Pageable pageable);
}
