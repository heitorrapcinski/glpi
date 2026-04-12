package com.glpi.asset.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for AssetStateDocument.
 */
public interface MongoAssetStateRepository extends MongoRepository<AssetStateDocument, String> {
}
