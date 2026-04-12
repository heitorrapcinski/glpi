package com.glpi.asset.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for SoftwareLicenseDocument.
 */
public interface MongoSoftwareLicenseRepository extends MongoRepository<SoftwareLicenseDocument, String> {

    List<SoftwareLicenseDocument> findAllBy(Pageable pageable);
}
