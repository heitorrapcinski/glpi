package com.glpi.asset.domain.port.in;

import com.glpi.asset.domain.model.Asset;

/**
 * Driving port — update an existing asset.
 * Requirements: 12.6
 */
public interface UpdateAssetUseCase {

    Asset updateAsset(UpdateAssetCommand cmd);
}
