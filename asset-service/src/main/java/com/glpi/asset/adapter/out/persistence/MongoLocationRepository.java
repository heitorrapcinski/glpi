package com.glpi.asset.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for LocationDocument.
 */
public interface MongoLocationRepository extends MongoRepository<LocationDocument, String> {
}
