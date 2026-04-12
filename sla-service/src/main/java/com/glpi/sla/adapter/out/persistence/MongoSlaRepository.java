package com.glpi.sla.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoSlaRepository extends MongoRepository<SlaDocument, String> {
}
