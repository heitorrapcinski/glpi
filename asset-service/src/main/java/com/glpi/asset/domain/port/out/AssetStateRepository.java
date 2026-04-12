package com.glpi.asset.domain.port.out;

import com.glpi.asset.domain.model.AssetState;

import java.util.List;
import java.util.Optional;

/**
 * Driven port — persistence contract for asset states.
 * Requirements: 12.3
 */
public interface AssetStateRepository {

    Optional<AssetState> findById(String id);

    AssetState save(AssetState state);

    List<AssetState> findAll();

    long count();
}
