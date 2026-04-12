package com.glpi.asset.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document for the software collection.
 * Requirements: 13.1
 */
@Document(collection = "software")
public class SoftwareDocument {

    @Id
    private String id;
    private String name;
    private String manufacturer;
    private String category;
    private List<String> versions = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public SoftwareDocument() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getVersions() { return versions; }
    public void setVersions(List<String> versions) { this.versions = versions; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
