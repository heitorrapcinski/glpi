package com.glpi.sla.domain.model;

public class SlaNotFoundException extends RuntimeException {
    public SlaNotFoundException(String id) {
        super("SLA not found: " + id);
    }
}
