package com.glpi.ticket.domain.model;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(String id) {
        super("Ticket not found: " + id);
    }
}
