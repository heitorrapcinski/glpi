package com.glpi.ticket.domain.model;

/**
 * ITIL ticket type: Incident or Service Request.
 * Requirements: 5.1
 */
public enum TicketType {
    INCIDENT(1),
    SERVICE_REQUEST(2);

    private final int value;

    TicketType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TicketType fromValue(int value) {
        for (TicketType t : values()) {
            if (t.value == value) return t;
        }
        throw new IllegalArgumentException("Unknown TicketType value: " + value);
    }
}
