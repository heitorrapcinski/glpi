package com.glpi.ticket.domain.model;

public class InsufficientRightsException extends RuntimeException {
    public InsufficientRightsException(String right) {
        super("Insufficient rights: missing " + right);
    }
}
