package com.glpi.knowledge.domain.model;

import java.time.Instant;

/**
 * Comment on a KB article.
 * Requirements: 17.8
 */
public class KnowbaseItemComment {

    private String id;
    private String content;
    private String authorId;
    private Instant createdAt;

    public KnowbaseItemComment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
