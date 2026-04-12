package com.glpi.problem.domain.model;

/**
 * Thrown when a status transition is not permitted.
 * Requirements: 26.4
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(ProblemStatus from, ProblemStatus to) {
        super("Invalid status transition from " + from + " to " + to);
    }
}
