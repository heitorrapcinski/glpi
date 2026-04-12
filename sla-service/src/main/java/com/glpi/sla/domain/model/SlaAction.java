package com.glpi.sla.domain.model;

import java.util.Map;

/**
 * Embedded action within an SLA escalation level.
 * actionType: "send_notification", "reassign", "change_priority"
 * Requirements: 14.3
 */
public class SlaAction {

    private String actionType;
    private Map<String, Object> parameters;

    public SlaAction() {}

    public SlaAction(String actionType, Map<String, Object> parameters) {
        this.actionType = actionType;
        this.parameters = parameters;
    }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}
