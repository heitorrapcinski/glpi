package com.glpi.identity.adapter.out.persistence;

import com.glpi.identity.domain.model.AuthType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB document mapping for the User aggregate.
 */
@Document(collection = "users")
@CompoundIndex(name = "username_authtype_unique", def = "{'username': 1, 'authType': 1}", unique = true)
public class UserDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String passwordHash;
    private AuthType authType;
    private String authSourceId;
    private List<EmailDocument> emails;

    @Indexed
    private boolean isActive;

    @Indexed
    private boolean isDeleted;

    @Indexed
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

    // Default constructor for Spring Data
    public UserDocument() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public AuthType getAuthType() { return authType; }
    public void setAuthType(AuthType authType) { this.authType = authType; }

    public String getAuthSourceId() { return authSourceId; }
    public void setAuthSourceId(String authSourceId) { this.authSourceId = authSourceId; }

    public List<EmailDocument> getEmails() { return emails; }
    public void setEmails(List<EmailDocument> emails) { this.emails = emails; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getPersonalToken() { return personalToken; }
    public void setPersonalToken(String personalToken) { this.personalToken = personalToken; }

    public String getApiToken() { return apiToken; }
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }

    public String getTotpSecret() { return totpSecret; }
    public void setTotpSecret(String totpSecret) { this.totpSecret = totpSecret; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public List<String> getPasswordHistory() { return passwordHistory; }
    public void setPasswordHistory(List<String> passwordHistory) { this.passwordHistory = passwordHistory; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Embedded email sub-document.
     */
    public static class EmailDocument {
        private String email;
        private boolean isDefault;

        public EmailDocument() {}

        public EmailDocument(String email, boolean isDefault) {
            this.email = email;
            this.isDefault = isDefault;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public boolean isDefault() { return isDefault; }
        public void setDefault(boolean aDefault) { isDefault = aDefault; }
    }
}
