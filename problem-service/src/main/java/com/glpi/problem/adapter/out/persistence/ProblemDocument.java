package com.glpi.problem.adapter.out.persistence;

import com.glpi.problem.domain.model.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document for the problems collection.
 * Requirements: 22.4
 */
@Document(collection = "problems")
public class ProblemDocument {

    @Id
    private String id;

    @Indexed
    private ProblemStatus status;

    private String title;
    private String content;

    @Indexed
    private String entityId;

    private int priority;
    private int urgency;
    private int impact;

    private List<Actor> actors = new ArrayList<>();

    @Indexed
    private List<String> linkedTicketIds = new ArrayList<>();

    private List<LinkedAsset> linkedAssets = new ArrayList<>();
    private String impactContent;
    private String causeContent;
    private String symptomContent;
    private List<Followup> followups = new ArrayList<>();
    private List<ProblemTask> tasks = new ArrayList<>();
    private Solution solution;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant solvedAt;
    private Instant closedAt;

    public ProblemDocument() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ProblemStatus getStatus() { return status; }
    public void setStatus(ProblemStatus status) { this.status = status; }

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

    public List<Actor> getActors() { return actors; }
    public void setActors(List<Actor> actors) { this.actors = actors; }

    public List<String> getLinkedTicketIds() { return linkedTicketIds; }
    public void setLinkedTicketIds(List<String> linkedTicketIds) { this.linkedTicketIds = linkedTicketIds; }

    public List<LinkedAsset> getLinkedAssets() { return linkedAssets; }
    public void setLinkedAssets(List<LinkedAsset> linkedAssets) { this.linkedAssets = linkedAssets; }

    public String getImpactContent() { return impactContent; }
    public void setImpactContent(String impactContent) { this.impactContent = impactContent; }

    public String getCauseContent() { return causeContent; }
    public void setCauseContent(String causeContent) { this.causeContent = causeContent; }

    public String getSymptomContent() { return symptomContent; }
    public void setSymptomContent(String symptomContent) { this.symptomContent = symptomContent; }

    public List<Followup> getFollowups() { return followups; }
    public void setFollowups(List<Followup> followups) { this.followups = followups; }

    public List<ProblemTask> getTasks() { return tasks; }
    public void setTasks(List<ProblemTask> tasks) { this.tasks = tasks; }

    public Solution getSolution() { return solution; }
    public void setSolution(Solution solution) { this.solution = solution; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getSolvedAt() { return solvedAt; }
    public void setSolvedAt(Instant solvedAt) { this.solvedAt = solvedAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
}
