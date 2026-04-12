package com.glpi.sla.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoOlaRepository extends MongoRepository<OlaDocument, String> {
}
