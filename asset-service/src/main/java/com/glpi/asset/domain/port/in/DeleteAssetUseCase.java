package com.glpi.asset.domain.port.in;

/**
 * Driving port — soft-delete an asset.
 * Requirements: 12.7
 */
public interface DeleteAssetUseCase {

    void deleteAsset(String id);
}
