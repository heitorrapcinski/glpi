package com.glpi.ticket.domain.model;

/**
 * Thrown when an operation is blocked by a pending validation.
 * Requirements: 8.3
 */
public class ValidationPendingException extends RuntimeException {
    public ValidationPendingException() {
        super("There are pending validations that must be resolved first");
    }
}
