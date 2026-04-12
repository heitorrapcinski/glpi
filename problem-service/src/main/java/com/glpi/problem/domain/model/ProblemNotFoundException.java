package com.glpi.problem.domain.model;

/**
 * Thrown when a problem cannot be found by ID.
 */
public class ProblemNotFoundException extends RuntimeException {

    public ProblemNotFoundException(String id) {
        super("Problem not found: " + id);
    }
}
