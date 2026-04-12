package com.glpi.notification.domain.model;

/**
 * Actor value object — represents an actor on a ticket/problem/change.
 * Used for notification target resolution.
 * Requirements: 16.5, 16.10
 */
public class Actor {

    private int actorType;
    private String actorKind;
    private String actorId;
    private boolean useNotification;

    public Actor() {}

    public Actor(int actorType, String actorKind, String actorId, boolean useNotification) {
        this.actorType = actorType;
        this.actorKind = actorKind;
        this.actorId = actorId;
        this.useNotification = useNotification;
    }

    public int getActorType() { return actorType; }
    public void setActorType(int actorType) { this.actorType = actorType; }

    public String getActorKind() { return actorKind; }
    public void setActorKind(String actorKind) { this.actorKind = actorKind; }

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public boolean isUseNotification() { return useNotification; }
    public void setUseNotification(boolean useNotification) { this.useNotification = useNotification; }
}
