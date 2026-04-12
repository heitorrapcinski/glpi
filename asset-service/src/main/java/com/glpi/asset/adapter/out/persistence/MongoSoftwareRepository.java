package com.glpi.asset.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for SoftwareDocument.
 */
public interface MongoSoftwareRepository extends MongoRepository<SoftwareDocument, String> {
}
