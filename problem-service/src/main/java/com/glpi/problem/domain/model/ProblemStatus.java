package com.glpi.problem.domain.model;

/**
 * ITIL Problem status values.
 * Requirements: 10.1
 */
public enum ProblemStatus {
    INCOMING(1),
    ASSIGNED(2),
    PLANNED(3),
    WAITING(4),
    SOLVED(5),
    CLOSED(6),
    ACCEPTED(7),
    OBSERVED(8);

    private final int value;

    ProblemStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ProblemStatus fromValue(int value) {
        for (ProblemStatus s : values()) {
            if (s.value == value) return s;
        }
        throw new IllegalArgumentException("Unknown ProblemStatus value: " + value);
    }
}
