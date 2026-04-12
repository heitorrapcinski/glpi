package com.glpi.identity.domain.model;

import java.util.List;

/**
 * Thrown when a password does not meet the complexity policy. Maps to HTTP 422.
 */
public class PasswordComplexityException extends RuntimeException {

    private final List<String> violations;

    public PasswordComplexityException(List<String> violations) {
        super("Password does not meet complexity requirements: " + violations);
        this.violations = List.copyOf(violations);
    }

    public List<String> getViolations() {
        return violations;
    }
}
