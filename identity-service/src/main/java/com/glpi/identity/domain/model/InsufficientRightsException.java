package com.glpi.identity.domain.model;

/**
 * Thrown when a user attempts an operation they do not have rights for.
 * Maps to HTTP 403 / INSUFFICIENT_RIGHTS.
 */
public class InsufficientRightsException extends RuntimeException {
    public InsufficientRightsException(String message) {
        super(message);
    }

    public InsufficientRightsException() {
        super("Insufficient rights to perform this operation");
    }
}
