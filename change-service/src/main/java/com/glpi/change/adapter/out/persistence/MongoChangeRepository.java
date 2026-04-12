package com.glpi.change.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for ChangeDocument.
 * Requirements: 22.5
 */
public interface MongoChangeRepository extends MongoRepository<ChangeDocument, String> {

    List<ChangeDocument> findAllBy(Pageable pageable);
}
