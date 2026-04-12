package com.glpi.ticket.domain.model;

import java.time.Instant;

/**
 * ITILFollowup embedded in a ticket.
 * Requirements: 7.1
 */
public class Followup {

    private String id;
    private String content;
    private String authorId;
    private boolean isPrivate;
    private String source;
    private Instant createdAt;

    public Followup() {}

    public Followup(String id, String content, String authorId,
                    boolean isPrivate, String source, Instant createdAt) {
        this.id = id;
        this.content = content;
        this.authorId = authorId;
        this.isPrivate = isPrivate;
        this.source = source;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
