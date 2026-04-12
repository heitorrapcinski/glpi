package com.glpi.change.domain.model;

import java.time.Instant;

/**
 * ITILTask embedded in a change.
 * Requirements: 11.1
 */
public class ChangeTask {

    private String id;
    private String content;
    private String assignedUserId;
    /** 1=TODO, 2=DONE */
    private int status;
    private boolean isPrivate;
    private Instant createdAt;

    public ChangeTask() {}

    public ChangeTask(String id, String content, String assignedUserId,
                      int status, boolean isPrivate, Instant createdAt) {
        this.id = id;
        this.content = content;
        this.assignedUserId = assignedUserId;
        this.status = status;
        this.isPrivate = isPrivate;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(String assignedUserId) { this.assignedUserId = assignedUserId; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
