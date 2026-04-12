package com.glpi.ticket.domain.model;

/**
 * Thrown when a SOLVED transition is attempted without a solution.
 * Requirements: 7.4
 */
public class SolutionRequiredException extends RuntimeException {
    public SolutionRequiredException() {
        super("A solution is required before transitioning to SOLVED status");
    }
}
