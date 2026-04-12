package com.glpi.ticket.adapter.out.persistence;

import com.glpi.ticket.domain.model.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document for the tickets collection.
 * Requirements: 22.3, 22.9
 */
@Document(collection = "tickets")
@CompoundIndex(name = "actors_actorId_idx", def = "{'actors.actorId': 1}")
public class TicketDocument {

    @Id
    private String id;

    private TicketType type;

    @Indexed
    private TicketStatus status;

    private String title;
    private String content;

    @Indexed
    private String entityId;

    private int priority;
    private int urgency;
    private int impact;
    private String categoryId;

    @Indexed
    private boolean isDeleted;

    private List<Actor> actors = new ArrayList<>();
    private List<Followup> followups = new ArrayList<>();
    private List<TicketTask> tasks = new ArrayList<>();
    private Solution solution;
    private List<Validation> validations = new ArrayList<>();
    private SlaContext sla;
    private boolean priorityManualOverride;

    @Indexed
    private Instant createdAt;

    private Instant updatedAt;
    private Instant solvedAt;
    private Instant closedAt;
    private Long takeIntoAccountDelay;

    public TicketDocument() {}

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
