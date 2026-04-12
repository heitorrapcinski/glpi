package com.glpi.sla.domain.model;

/**
 * SLA type: TTO = Time To Own (assignment deadline), TTR = Time To Resolve.
 * Requirements: 14.1
 */
public enum SlaType {
    TTO(1),
    TTR(2);

    private final int value;

    SlaType(int value) { this.value = value; }

    public int getValue() { return value; }

    public static SlaType fromValue(int value) {
        for (SlaType t : values()) {
            if (t.value == value) return t;
        }
        throw new IllegalArgumentException("Unknown SlaType value: " + value);
    }
}
