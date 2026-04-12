package com.glpi.identity.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for GroupDocument.
 */
public interface MongoGroupRepository extends MongoRepository<GroupDocument, String> {

    List<GroupDocument> findByEntityId(String entityId);
}
