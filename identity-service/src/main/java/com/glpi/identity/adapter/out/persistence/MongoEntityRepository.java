package com.glpi.identity.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for EntityDocument.
 */
public interface MongoEntityRepository extends MongoRepository<EntityDocument, String> {

    List<EntityDocument> findByParentId(String parentId);

    boolean existsByNameAndParentId(String name, String parentId);
}
