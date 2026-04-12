package com.glpi.change.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Change aggregate root — represents an ITIL Change.
 * Requirements: 11.1, 11.3, 11.4, 11.6, 11.8, 11.10, 22.5
 */
public class Change {

    private String id;
    private ChangeStatus status;
    private String title;
    private String content;
    private String entityId;
    private int priority;
    private int urgency;
    private int impact;
    private List<Actor> actors;
    private PlanningDocuments planningDocuments;
    private List<ValidationStep> validationSteps;
    private List<String> linkedTicketIds;
    private List<String> linkedProblemIds;
    private List<LinkedAsset> linkedAssets;
    private List<Followup> followups;
    private List<ChangeTask> tasks;
    private Solution solution;
    private SatisfactionSurvey satisfactionSurvey;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;

    public Change() {
        this.actors = new ArrayList<>();
        this.validationSteps = new ArrayList<>();
        this.linkedTicketIds = new ArrayList<>();
        this.linkedProblemIds = new ArrayList<>();
        this.linkedAssets = new ArrayList<>();
        this.followups = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.planningDocuments = new PlanningDocuments();
    }

    // Getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ChangeStatus getStatus() { return status; }
    public void setStatus(ChangeStatus status) { this.status = status; }

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

    public PlanningDocuments getPlanningDocuments() { return planningDocuments; }
    public void setPlanningDocuments(PlanningDocuments planningDocuments) { this.planningDocuments = planningDocuments; }

    public List<ValidationStep> getValidationSteps() { return validationSteps; }
    public void setValidationSteps(List<ValidationStep> validationSteps) { this.validationSteps = validationSteps; }

    public List<String> getLinkedTicketIds() { return linkedTicketIds; }
    public void setLinkedTicketIds(List<String> linkedTicketIds) { this.linkedTicketIds = linkedTicketIds; }

    public List<String> getLinkedProblemIds() { return linkedProblemIds; }
    public void setLinkedProblemIds(List<String> linkedProblemIds) { this.linkedProblemIds = linkedProblemIds; }

    public List<LinkedAsset> getLinkedAssets() { return linkedAssets; }
    public void setLinkedAssets(List<LinkedAsset> linkedAssets) { this.linkedAssets = linkedAssets; }

    public List<Followup> getFollowups() { return followups; }
    public void setFollowups(List<Followup> followups) { this.followups = followups; }

    public List<ChangeTask> getTasks() { return tasks; }
    public void setTasks(List<ChangeTask> tasks) { this.tasks = tasks; }

    public Solution getSolution() { return solution; }
    public void setSolution(Solution solution) { this.solution = solution; }

    public SatisfactionSurvey getSatisfactionSurvey() { return satisfactionSurvey; }
    public void setSatisfactionSurvey(SatisfactionSurvey satisfactionSurvey) { this.satisfactionSurvey = satisfactionSurvey; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
}
