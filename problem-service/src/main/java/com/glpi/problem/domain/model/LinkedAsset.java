package com.glpi.problem.domain.model;

/**
 * LinkedAsset value object — a CI linked to a problem.
 * Requirements: 10.7
 */
public class LinkedAsset {

    private String assetType;
    private String assetId;

    public LinkedAsset() {}

    public LinkedAsset(String assetType, String assetId) {
        this.assetType = assetType;
        this.assetId = assetId;
    }

    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }

    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }
}
