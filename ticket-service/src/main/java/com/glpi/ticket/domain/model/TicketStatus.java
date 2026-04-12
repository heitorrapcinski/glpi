package com.glpi.ticket.domain.model;

/**
 * ITIL ticket lifecycle statuses.
 * Requirements: 5.2
 */
public enum TicketStatus {
    INCOMING(1),
    ASSIGNED(2),
    PLANNED(3),
    WAITING(4),
    SOLVED(5),
    CLOSED(6);

    private final int value;

    TicketStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TicketStatus fromValue(int value) {
        for (TicketStatus s : values()) {
            if (s.value == value) return s;
        }
        throw new IllegalArgumentException("Unknown TicketStatus value: " + value);
    }
}
