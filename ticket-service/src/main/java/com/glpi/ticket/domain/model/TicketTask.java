package com.glpi.ticket.domain.model;

import java.time.Instant;

/**
 * ITILTask embedded in a ticket.
 * Requirements: 7.2
 */
public class TicketTask {

    private String id;
    private String content;
    private String assignedUserId;
    /** 1=TODO, 2=DONE */
    private int status;
    private boolean isPrivate;
    private Instant plannedStart;
    private Instant plannedEnd;
    private long duration;
    private Instant createdAt;

    public TicketTask() {}

    public TicketTask(String id, String content, String assignedUserId, int status,
                      boolean isPrivate, Instant plannedStart, Instant plannedEnd,
                      long duration, Instant createdAt) {
        this.id = id;
        this.content = content;
        this.assignedUserId = assignedUserId;
        this.status = status;
        this.isPrivate = isPrivate;
        this.plannedStart = plannedStart;
        this.plannedEnd = plannedEnd;
        this.duration = duration;
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

    public Instant getPlannedStart() { return plannedStart; }
    public void setPlannedStart(Instant plannedStart) { this.plannedStart = plannedStart; }

    public Instant getPlannedEnd() { return plannedEnd; }
    public void setPlannedEnd(Instant plannedEnd) { this.plannedEnd = plannedEnd; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
