package com.glpi.change.domain.model;

/**
 * Actor value object — a stakeholder on a change.
 * Requirements: 11.8
 */
public class Actor {

    private ActorType actorType;
    /** "user", "group", or "supplier" */
    private String actorKind;
    private String actorId;
    private boolean useNotification;

    public Actor() {}

    public Actor(ActorType actorType, String actorKind, String actorId, boolean useNotification) {
        this.actorType = actorType;
        this.actorKind = actorKind;
        this.actorId = actorId;
        this.useNotification = useNotification;
    }

    public ActorType getActorType() { return actorType; }
    public void setActorType(ActorType actorType) { this.actorType = actorType; }

    public String getActorKind() { return actorKind; }
    public void setActorKind(String actorKind) { this.actorKind = actorKind; }

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public boolean isUseNotification() { return useNotification; }
    public void setUseNotification(boolean useNotification) { this.useNotification = useNotification; }
}
