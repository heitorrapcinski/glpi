package com.glpi.asset.domain.model;

import java.time.Instant;

/**
 * Software license entity for compliance tracking.
 * Requirements: 13.2
 */
public class SoftwareLicense {

    private String id;
    private String name;
    private String softwareId;
    private String licenseType;
    private String serial;
    private int numberOfSeats;
    private Instant expiryDate;
    private String entityId;
    private Instant createdAt;
    private Instant updatedAt;

    public SoftwareLicense() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSoftwareId() { return softwareId; }
    public void setSoftwareId(String softwareId) { this.softwareId = softwareId; }

    public String getLicenseType() { return licenseType; }
    public void setLicenseType(String licenseType) { this.licenseType = licenseType; }

    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }

    public int getNumberOfSeats() { return numberOfSeats; }
    public void setNumberOfSeats(int numberOfSeats) { this.numberOfSeats = numberOfSeats; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
