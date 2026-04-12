package com.glpi.asset.domain.port.in;

import com.glpi.asset.domain.model.Asset;

/**
 * Driving port — create a new asset.
 * Requirements: 12.5
 */
public interface CreateAssetUseCase {

    Asset createAsset(CreateAssetCommand cmd);
}
