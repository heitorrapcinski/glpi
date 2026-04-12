package com.glpi.ticket.domain.model;

/**
 * Actor value object — a stakeholder on a ticket.
 * Requirements: 6.1, 6.2, 6.3, 6.4
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
