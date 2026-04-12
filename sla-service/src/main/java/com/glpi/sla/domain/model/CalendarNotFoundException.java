package com.glpi.sla.domain.model;

public class CalendarNotFoundException extends RuntimeException {
    public CalendarNotFoundException(String id) {
        super("Calendar not found: " + id);
    }
}
