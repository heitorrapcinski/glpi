package com.glpi.knowledge.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for KnowbaseItemCategoryDocument.
 * Requirements: 17.2
 */
public interface MongoKnowbaseItemCategoryRepository extends MongoRepository<KnowbaseItemCategoryDocument, String> {

    List<KnowbaseItemCategoryDocument> findAllBy(Pageable pageable);
}
