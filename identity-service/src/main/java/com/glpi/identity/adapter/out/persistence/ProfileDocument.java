package com.glpi.identity.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * MongoDB document mapping for the Profile aggregate.
 */
@Document(collection = "profiles")
public class ProfileDocument {

    @Id
    private String id;

    private String name;
    private String interface_;
    private boolean isDefault;
    private boolean twoFactorEnforced;
    private Map<String, Integer> rights;
    private TicketStatusMatrixDocument ticketStatusMatrix;
    private Instant createdAt;
    private Instant updatedAt;

    public ProfileDocument() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getInterface() { return interface_; }
    public void setInterface(String interface_) { this.interface_ = interface_; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public boolean isTwoFactorEnforced() { return twoFactorEnforced; }
    public void setTwoFactorEnforced(boolean twoFactorEnforced) { this.twoFactorEnforced = twoFactorEnforced; }

    public Map<String, Integer> getRights() { return rights; }
    public void setRights(Map<String, Integer> rights) { this.rights = rights; }

    public TicketStatusMatrixDocument getTicketStatusMatrix() { return ticketStatusMatrix; }
    public void setTicketStatusMatrix(TicketStatusMatrixDocument ticketStatusMatrix) {
        this.ticketStatusMatrix = ticketStatusMatrix;
    }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Embedded ticket status matrix sub-document.
     */
    public static class TicketStatusMatrixDocument {
        private List<int[]> allowed;

        public TicketStatusMatrixDocument() {}

        public List<int[]> getAllowed() { return allowed; }
        public void setAllowed(List<int[]> allowed) { this.allowed = allowed; }
    }
}
