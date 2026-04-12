package com.glpi.identity.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB document mapping for the Group aggregate.
 */
@Document(collection = "groups")
public class GroupDocument {

    @Id
    private String id;

    private String name;

    @Indexed
    private String entityId;

    private boolean isRecursive;
    private List<String> memberUserIds;
    private Instant createdAt;
    private Instant updatedAt;

    public GroupDocument() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public boolean isRecursive() { return isRecursive; }
    public void setRecursive(boolean recursive) { isRecursive = recursive; }

    public List<String> getMemberUserIds() { return memberUserIds; }
    public void setMemberUserIds(List<String> memberUserIds) { this.memberUserIds = memberUserIds; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
