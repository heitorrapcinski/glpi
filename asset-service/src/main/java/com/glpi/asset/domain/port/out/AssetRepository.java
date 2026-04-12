package com.glpi.asset.domain.port.out;

import com.glpi.asset.domain.model.Asset;
import com.glpi.asset.domain.model.AssetType;

import java.util.List;
import java.util.Optional;

/**
 * Driven port — persistence contract for Asset aggregate.
 * Requirements: 22.6
 */
public interface AssetRepository {

    Optional<Asset> findById(String id);

    Asset save(Asset asset);

    void delete(String id);

    List<Asset> findByType(AssetType type, int page, int size);

    long countByType(AssetType type);

    List<Asset> findAllNotDeleted(int page, int size);

    long countAllNotDeleted();
}
