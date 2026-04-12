package com.glpi.asset.domain.model;

import java.time.Instant;

/**
 * Configurable asset state in the lifecycle.
 * Requirements: 12.3
 */
public class AssetState {

    private String id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;

    public AssetState() {}

    public AssetState(String id, String name) {
        this.id = id;
        this.name = name;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
