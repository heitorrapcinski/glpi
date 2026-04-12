package com.glpi.identity.domain.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Profile aggregate root representing a named role with fine-grained RBAC permissions.
 */
public class Profile {

    /** Standard CRUD permission bits. */
    public static final int RIGHT_READ = 1;
    public static final int RIGHT_UPDATE = 2;
    public static final int RIGHT_CREATE = 4;
    public static final int RIGHT_DELETE = 8;
    public static final int RIGHT_PURGE = 16;

    private String id;
    private String name;
    /** Profile interface: "central" or "helpdesk". */
    private String interface_;
    private boolean isDefault;
    private boolean twoFactorEnforced;
    /** Bitfield rights per resource name (e.g. "ticket" -> 31). */
    private Map<String, Integer> rights;
    private TicketStatusMatrix ticketStatusMatrix;
    private Instant createdAt;
    private Instant updatedAt;

    public Profile(
            String id,
            String name,
            String interface_,
            boolean isDefault,
            boolean twoFactorEnforced,
            Map<String, Integer> rights,
            TicketStatusMatrix ticketStatusMatrix) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Profile name must not be null or blank");
        }
        if (interface_ == null || (!interface_.equals("central") && !interface_.equals("helpdesk"))) {
            throw new IllegalArgumentException("Profile interface must be 'central' or 'helpdesk'");
        }

        this.id = id;
        this.name = name;
        this.interface_ = interface_;
        this.isDefault = isDefault;
        this.twoFactorEnforced = twoFactorEnforced;
        this.rights = rights != null ? new HashMap<>(rights) : new HashMap<>();
        this.ticketStatusMatrix = ticketStatusMatrix != null ? ticketStatusMatrix : new TicketStatusMatrix();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getInterface() { return interface_; }
    public boolean isDefault() { return isDefault; }
    public boolean isTwoFactorEnforced() { return twoFactorEnforced; }
    public Map<String, Integer> getRights() { return Map.copyOf(rights); }
    public TicketStatusMatrix getTicketStatusMatrix() { return ticketStatusMatrix; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setInterface(String interface_) {
        this.interface_ = interface_;
        this.updatedAt = Instant.now();
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
        this.updatedAt = Instant.now();
    }

    public void setTwoFactorEnforced(boolean twoFactorEnforced) {
        this.twoFactorEnforced = twoFactorEnforced;
        this.updatedAt = Instant.now();
    }

    public void setRights(Map<String, Integer> rights) {
        this.rights = rights != null ? new HashMap<>(rights) : new HashMap<>();
        this.updatedAt = Instant.now();
    }

    public void setTicketStatusMatrix(TicketStatusMatrix ticketStatusMatrix) {
        this.ticketStatusMatrix = ticketStatusMatrix;
        this.updatedAt = Instant.now();
    }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Returns true if this profile holds the UPDATE right on the given resource.
     */
    public boolean hasUpdateRightOn(String resource) {
        Integer bits = rights.get(resource);
        return bits != null && (bits & RIGHT_UPDATE) != 0;
    }
}
