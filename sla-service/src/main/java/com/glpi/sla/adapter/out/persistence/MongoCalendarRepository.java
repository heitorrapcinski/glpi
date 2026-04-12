package com.glpi.sla.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for CalendarDocument.
 */
public interface MongoCalendarRepository extends MongoRepository<CalendarDocument, String> {
}
