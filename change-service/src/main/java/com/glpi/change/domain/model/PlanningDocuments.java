package com.glpi.change.domain.model;

/**
 * Planning documents embedded in a change.
 * Requirements: 11.3
 */
public class PlanningDocuments {

    private String impactContent;
    private String controlListContent;
    private String rolloutPlanContent;
    private String backoutPlanContent;
    private String checklistContent;

    public PlanningDocuments() {}

    public String getImpactContent() { return impactContent; }
    public void setImpactContent(String impactContent) { this.impactContent = impactContent; }

    public String getControlListContent() { return controlListContent; }
    public void setControlListContent(String controlListContent) { this.controlListContent = controlListContent; }

    public String getRolloutPlanContent() { return rolloutPlanContent; }
    public void setRolloutPlanContent(String rolloutPlanContent) { this.rolloutPlanContent = rolloutPlanContent; }

    public String getBackoutPlanContent() { return backoutPlanContent; }
    public void setBackoutPlanContent(String backoutPlanContent) { this.backoutPlanContent = backoutPlanContent; }

    public String getChecklistContent() { return checklistContent; }
    public void setChecklistContent(String checklistContent) { this.checklistContent = checklistContent; }
}
