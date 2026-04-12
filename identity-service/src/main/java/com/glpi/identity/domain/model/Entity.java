package com.glpi.identity.domain.model;

import java.time.Instant;

/**
 * Entity aggregate root representing an organizational unit in the multi-tenant hierarchy.
 * Entities form a recursive tree; the root entity has id="0" and no parent.
 */
public class Entity {

    private String id;
    private String name;
    private String parentId;
    private int level;
    private String completeName;
    private EntityConfig config;
    private Instant createdAt;
    private Instant updatedAt;

    public Entity(
            String id,
            String name,
            String parentId,
            int level,
            String completeName,
            EntityConfig config) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Entity name must not be null or blank");
        }
        if (level < 1) {
            throw new IllegalArgumentException("Entity level must be >= 1");
        }

        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.level = level;
        this.completeName = completeName;
        this.config = config != null ? config : new EntityConfig();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getParentId() { return parentId; }
    public int getLevel() { return level; }
    public String getCompleteName() { return completeName; }
    public EntityConfig getConfig() { return config; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setParentId(String parentId) { this.parentId = parentId; }

    public void setLevel(int level) { this.level = level; }

    public void setCompleteName(String completeName) { this.completeName = completeName; }

    public void setConfig(EntityConfig config) {
        this.config = config;
        this.updatedAt = Instant.now();
    }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
