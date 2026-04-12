package com.glpi.identity.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for refresh tokens stored in the {@code refresh_tokens} collection.
 */
@Document(collection = "refresh_tokens")
public class RefreshTokenDocument {

    @Id
    private String id;

    @Indexed
    private String userId;

    /** SHA-256 hash of the opaque token value. */
    @Indexed(unique = true)
    private String tokenHash;

    /** Family ID groups all tokens issued from the same original login. */
    @Indexed
    private String familyId;

    private Instant expiresAt;
    private boolean isRevoked;
    private Instant createdAt;

    public RefreshTokenDocument() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public boolean isRevoked() { return isRevoked; }
    public void setRevoked(boolean revoked) { isRevoked = revoked; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
