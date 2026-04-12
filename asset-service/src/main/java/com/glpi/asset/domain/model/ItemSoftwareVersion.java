package com.glpi.asset.domain.model;

import java.time.Instant;

/**
 * Links a software version to a specific asset (installation record).
 * Requirements: 13.3
 */
public class ItemSoftwareVersion {

    private String id;
    private String assetId;
    private String softwareId;
    private String version;
    private String licenseId;
    private Instant installedAt;

    public ItemSoftwareVersion() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }

    public String getSoftwareId() { return softwareId; }
    public void setSoftwareId(String softwareId) { this.softwareId = softwareId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getLicenseId() { return licenseId; }
    public void setLicenseId(String licenseId) { this.licenseId = licenseId; }

    public Instant getInstalledAt() { return installedAt; }
    public void setInstalledAt(Instant installedAt) { this.installedAt = installedAt; }
}
