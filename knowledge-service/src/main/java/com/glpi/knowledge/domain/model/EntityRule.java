package com.glpi.knowledge.domain.model;

/**
 * Entity visibility rule — specifies an entity and whether visibility is recursive to children.
 * Requirements: 17.3
 */
public class EntityRule {

    private String entityId;
    private boolean isRecursive;

    public EntityRule() {}

    public EntityRule(String entityId, boolean isRecursive) {
        this.entityId = entityId;
        this.isRecursive = isRecursive;
    }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public boolean isRecursive() { return isRecursive; }
    public void setRecursive(boolean recursive) { isRecursive = recursive; }
}
