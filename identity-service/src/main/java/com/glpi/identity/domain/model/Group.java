package com.glpi.identity.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Group aggregate root representing a collection of users that can be assigned as actors on ITIL objects.
 */
public class Group {

    private String id;
    private String name;
    private String entityId;
    private boolean isRecursive;
    private List<String> memberUserIds;
    private Instant createdAt;
    private Instant updatedAt;

    public Group(
            String id,
            String name,
            String entityId,
            boolean isRecursive,
            List<String> memberUserIds) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Group name must not be null or blank");
        }

        this.id = id;
        this.name = name;
        this.entityId = entityId;
        this.isRecursive = isRecursive;
        this.memberUserIds = memberUserIds != null ? new ArrayList<>(memberUserIds) : new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEntityId() { return entityId; }
    public boolean isRecursive() { return isRecursive; }
    public List<String> getMemberUserIds() { return List.copyOf(memberUserIds); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
        this.updatedAt = Instant.now();
    }

    public void setRecursive(boolean recursive) {
        this.isRecursive = recursive;
        this.updatedAt = Instant.now();
    }

    public void setMemberUserIds(List<String> memberUserIds) {
        this.memberUserIds = memberUserIds != null ? new ArrayList<>(memberUserIds) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
