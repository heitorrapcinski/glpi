package com.glpi.identity.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * User aggregate root for the Identity bounded context.
 */
public class User {

    private String id;
    private String username;
    private String passwordHash;
    private AuthType authType;
    private String authSourceId;
    private List<Email> emails;
    private boolean isActive;
    private boolean isDeleted;
    private String entityId;
    private String profileId;
    private String language;
    private String personalToken;
    private String apiToken;
    private String totpSecret;
    private boolean twoFactorEnabled;
    private List<String> passwordHistory;
    private int failedLoginAttempts;
    private Instant lockedUntil;
    private Instant createdAt;
    private Instant updatedAt;

    public User(
            String id,
            String username,
            String passwordHash,
            AuthType authType,
            String authSourceId,
            List<Email> emails,
            String entityId,
            String profileId) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be null or blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be null or blank");
        }
        if (emails == null || emails.isEmpty()) {
            throw new IllegalArgumentException("At least one email address is required");
        }
        if (authType == null) {
            throw new IllegalArgumentException("AuthType must not be null");
        }

        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.authType = authType;
        this.authSourceId = authSourceId;
        this.emails = new ArrayList<>(emails);
        this.isActive = true;
        this.isDeleted = false;
        this.entityId = entityId;
        this.profileId = profileId;
        this.language = "en_US";
        this.twoFactorEnabled = false;
        this.passwordHistory = new ArrayList<>();
        this.failedLoginAttempts = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public AuthType getAuthType() { return authType; }
    public String getAuthSourceId() { return authSourceId; }
    public List<Email> getEmails() { return List.copyOf(emails); }
    public boolean isActive() { return isActive; }
    public boolean isDeleted() { return isDeleted; }
    public String getEntityId() { return entityId; }
    public String getProfileId() { return profileId; }
    public String getLanguage() { return language; }
    public String getPersonalToken() { return personalToken; }
    public String getApiToken() { return apiToken; }
    public String getTotpSecret() { return totpSecret; }
    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public List<String> getPasswordHistory() { return List.copyOf(passwordHistory); }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public Instant getLockedUntil() { return lockedUntil; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Setters / domain mutators
    public void setId(String id) { this.id = id; }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        this.isDeleted = true;
        this.updatedAt = Instant.now();
    }

    public void updatePasswordHash(String newHash) {
        if (newHash == null || newHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be null or blank");
        }
        this.passwordHash = newHash;
        this.updatedAt = Instant.now();
    }

    public void setPersonalToken(String personalToken) {
        this.personalToken = personalToken;
        this.updatedAt = Instant.now();
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
        this.updatedAt = Instant.now();
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
        this.updatedAt = Instant.now();
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
        this.updatedAt = Instant.now();
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        this.updatedAt = Instant.now();
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = Instant.now();
    }

    public void lockUntil(Instant until) {
        this.lockedUntil = until;
        this.updatedAt = Instant.now();
    }

    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    public void setLanguage(String language) {
        this.language = language;
        this.updatedAt = Instant.now();
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
        this.updatedAt = Instant.now();
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
        this.updatedAt = Instant.now();
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setActive(boolean active) {
        this.isActive = active;
        this.updatedAt = Instant.now();
    }

    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
        this.updatedAt = Instant.now();
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public void setPasswordHistory(List<String> passwordHistory) {
        this.passwordHistory = new ArrayList<>(passwordHistory);
    }
}
