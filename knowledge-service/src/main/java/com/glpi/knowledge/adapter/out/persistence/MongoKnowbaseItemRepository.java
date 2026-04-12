package com.glpi.knowledge.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for KnowbaseItemDocument.
 * Requirements: 17.1
 */
public interface MongoKnowbaseItemRepository extends MongoRepository<KnowbaseItemDocument, String> {

    List<KnowbaseItemDocument> findAllBy(Pageable pageable);
}
