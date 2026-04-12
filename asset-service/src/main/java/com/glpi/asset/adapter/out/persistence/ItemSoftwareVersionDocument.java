package com.glpi.asset.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for software installation records.
 * Requirements: 13.3
 */
@Document(collection = "item_software_versions")
public class ItemSoftwareVersionDocument {

    @Id
    private String id;

    @Indexed
    private String assetId;

    @Indexed
    private String softwareId;

    private String version;

    @Indexed
    private String licenseId;

    private Instant installedAt;

    public ItemSoftwareVersionDocument() {}

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
