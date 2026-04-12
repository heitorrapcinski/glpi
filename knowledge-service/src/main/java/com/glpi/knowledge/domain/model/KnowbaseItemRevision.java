package com.glpi.knowledge.domain.model;

import java.time.Instant;

/**
 * Revision entry tracking article edit history.
 * Requirements: 17.7
 */
public class KnowbaseItemRevision {

    private String id;
    private String oldTitle;
    private String oldAnswer;
    private String authorId;
    private Instant createdAt;

    public KnowbaseItemRevision() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOldTitle() { return oldTitle; }
    public void setOldTitle(String oldTitle) { this.oldTitle = oldTitle; }

    public String getOldAnswer() { return oldAnswer; }
    public void setOldAnswer(String oldAnswer) { this.oldAnswer = oldAnswer; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
