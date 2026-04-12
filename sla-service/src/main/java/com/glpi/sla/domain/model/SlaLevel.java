package com.glpi.sla.domain.model;

import java.util.List;

/**
 * Embedded escalation level within an SLA/OLA.
 * executionDelaySeconds: negative means "before deadline", positive means "after deadline".
 * Requirements: 14.3
 */
public class SlaLevel {

    private String id;
    private String name;
    private long executionDelaySeconds;
    private List<SlaAction> actions;

    public SlaLevel() {}

    public SlaLevel(String id, String name, long executionDelaySeconds, List<SlaAction> actions) {
        this.id = id;
        this.name = name;
        this.executionDelaySeconds = executionDelaySeconds;
        this.actions = actions;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getExecutionDelaySeconds() { return executionDelaySeconds; }
    public void setExecutionDelaySeconds(long executionDelaySeconds) {
        this.executionDelaySeconds = executionDelaySeconds;
    }

    public List<SlaAction> getActions() { return actions; }
    public void setActions(List<SlaAction> actions) { this.actions = actions; }
}
