package com.glpi.sla.domain.model;

public class OlaNotFoundException extends RuntimeException {
    public OlaNotFoundException(String id) {
        super("OLA not found: " + id);
    }
}
