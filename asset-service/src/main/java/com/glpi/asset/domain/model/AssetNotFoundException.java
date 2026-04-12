package com.glpi.asset.domain.model;

/**
 * Thrown when an asset is not found by ID.
 */
public class AssetNotFoundException extends RuntimeException {

    public AssetNotFoundException(String id) {
        super("Asset not found: " + id);
    }
}
