package com.glpi.change.domain.model;

/**
 * Actor type on a change.
 * Requirements: 11.8
 */
public enum ActorType {
    REQUESTER(1),
    ASSIGNED(2),
    OBSERVER(3),
    SUPPLIER(4);

    private final int value;

    ActorType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
