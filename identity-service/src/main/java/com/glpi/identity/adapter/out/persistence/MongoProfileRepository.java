package com.glpi.identity.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for ProfileDocument.
 */
public interface MongoProfileRepository extends MongoRepository<ProfileDocument, String> {

    Optional<ProfileDocument> findByIsDefaultTrue();
}
