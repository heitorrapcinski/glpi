package com.glpi.problem.domain.model;

/**
 * Actor type on a problem.
 * Requirements: 10.8
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
