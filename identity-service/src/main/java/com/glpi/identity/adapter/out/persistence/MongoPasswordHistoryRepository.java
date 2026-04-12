package com.glpi.identity.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for password history entries.
 */
public interface MongoPasswordHistoryRepository extends MongoRepository<PasswordHistoryDocument, String> {

    List<PasswordHistoryDocument> findByUserIdOrderByCreatedAtDesc(String userId);
}
