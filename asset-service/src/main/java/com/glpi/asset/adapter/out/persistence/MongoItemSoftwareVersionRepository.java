package com.glpi.asset.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for ItemSoftwareVersionDocument.
 */
public interface MongoItemSoftwareVersionRepository extends MongoRepository<ItemSoftwareVersionDocument, String> {

    List<ItemSoftwareVersionDocument> findByAssetId(String assetId);

    List<ItemSoftwareVersionDocument> findByLicenseId(String licenseId);

    long countByLicenseId(String licenseId);
}
