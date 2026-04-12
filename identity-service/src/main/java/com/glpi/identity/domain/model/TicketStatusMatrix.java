package com.glpi.identity.domain.model;

import java.util.List;

/**
 * Embedded value object representing the allowed ticket status transitions for a profile.
 * Each entry in 'allowed' is a two-element int array: [fromStatus, toStatus].
 */
public class TicketStatusMatrix {

    private List<int[]> allowed;

    public TicketStatusMatrix() {
        this.allowed = List.of();
    }

    public TicketStatusMatrix(List<int[]> allowed) {
        this.allowed = allowed != null ? List.copyOf(allowed) : List.of();
    }

    public List<int[]> getAllowed() { return allowed; }

    public void setAllowed(List<int[]> allowed) {
        this.allowed = allowed != null ? List.copyOf(allowed) : List.of();
    }
}
