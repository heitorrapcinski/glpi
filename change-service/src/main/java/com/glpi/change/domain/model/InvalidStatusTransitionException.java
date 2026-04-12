package com.glpi.change.domain.model;

/**
 * Thrown when a status transition is not permitted.
 * Requirements: 26.3
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(ChangeStatus from, ChangeStatus to) {
        super("Invalid status transition from " + from + " to " + to);
    }
}
