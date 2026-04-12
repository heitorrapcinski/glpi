package com.glpi.ticket.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket aggregate root — represents an ITIL Incident or Service Request.
 * Requirements: 5.1, 5.2, 6.1, 6.2, 6.3, 6.4, 22.3
 */
public class Ticket {

    private String id;
    private TicketType type;
    private TicketStatus status;
    private String title;
    private String content;
    private String entityId;
    private int priority;
    private int urgency;
    private int impact;
    private String categoryId;
    private boolean isDeleted;
    private List<Actor> actors;
    private List<Followup> followups;
    private List<TicketTask> tasks;
    private Solution solution;
    private List<Validation> validations;
    private SlaContext sla;
    private boolean priorityManualOverride;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant solvedAt;
    private Instant closedAt;
    private Long takeIntoAccountDelay;

    public Ticket() {
        this.actors = new ArrayList<>();
        this.followups = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.validations = new ArrayList<>();
    }

    // Getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public TicketType getType() { return type; }
    public void setType(TicketType type) { this.type = type; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public int getUrgency() { return urgency; }
    public void setUrgency(int urgency) { this.urgency = urgency; }

    public int getImpact() { return impact; }
    public void setImpact(int impact) { this.impact = impact; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public List<Actor> getActors() { return actors; }
    public void setActors(List<Actor> actors) { this.actors = actors; }

    public List<Followup> getFollowups() { return followups; }
    public void setFollowups(List<Followup> followups) { this.followups = followups; }

    public List<TicketTask> getTasks() { return tasks; }
    public void setTasks(List<TicketTask> tasks) { this.tasks = tasks; }

    public Solution getSolution() { return solution; }
    public void setSolution(Solution solution) { this.solution = solution; }

    public List<Validation> getValidations() { return validations; }
    public void setValidations(List<Validation> validations) { this.validations = validations; }

    public SlaContext getSla() { return sla; }
    public void setSla(SlaContext sla) { this.sla = sla; }

    public boolean isPriorityManualOverride() { return priorityManualOverride; }
    public void setPriorityManualOverride(boolean priorityManualOverride) {
        this.priorityManualOverride = priorityManualOverride;
    }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getSolvedAt() { return solvedAt; }
    public void setSolvedAt(Instant solvedAt) { this.solvedAt = solvedAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }

    public Long getTakeIntoAccountDelay() { return takeIntoAccountDelay; }
    public void setTakeIntoAccountDelay(Long takeIntoAccountDelay) {
        this.takeIntoAccountDelay = takeIntoAccountDelay;
    }
}
