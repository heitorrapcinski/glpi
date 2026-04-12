package com.glpi.asset.adapter.out.persistence;

import com.glpi.asset.domain.model.AssetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for AssetDocument.
 * Requirements: 22.6
 */
public interface MongoAssetRepository extends MongoRepository<AssetDocument, String> {

    List<AssetDocument> findByAssetTypeAndIsDeletedFalse(AssetType assetType, Pageable pageable);

    long countByAssetTypeAndIsDeletedFalse(AssetType assetType);

    List<AssetDocument> findByIsDeletedFalse(Pageable pageable);

    long countByIsDeletedFalse();
}
