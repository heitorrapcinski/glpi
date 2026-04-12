package com.glpi.ticket.domain.model;

/**
 * Actor role on a ticket.
 * Requirements: 6.1
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

    public static ActorType fromValue(int value) {
        for (ActorType a : values()) {
            if (a.value == value) return a;
        }
        throw new IllegalArgumentException("Unknown ActorType value: " + value);
    }
}
