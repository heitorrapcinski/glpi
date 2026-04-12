package com.glpi.change.domain.model;

/**
 * ITIL Change status values.
 * Requirements: 11.1
 */
public enum ChangeStatus {
    INCOMING(1),
    ASSIGNED(2),
    PLANNED(3),
    WAITING(4),
    SOLVED(5),
    CLOSED(6),
    ACCEPTED(7),
    OBSERVED(8),
    EVALUATION(9),
    APPROVAL(10),
    TEST(11),
    QUALIFICATION(12),
    REFUSED(13),
    CANCELED(14);

    private final int value;

    ChangeStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ChangeStatus fromValue(int value) {
        for (ChangeStatus s : values()) {
            if (s.value == value) return s;
        }
        throw new IllegalArgumentException("Unknown ChangeStatus value: " + value);
    }
}
