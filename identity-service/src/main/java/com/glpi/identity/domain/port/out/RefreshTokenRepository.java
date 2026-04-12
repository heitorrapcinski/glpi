package com.glpi.identity.domain.port.out;

import java.time.Instant;
import java.util.Optional;

/**
 * Driven port: persistence contract for refresh tokens.
 */
public interface RefreshTokenRepository {

    /**
     * Stores a new refresh token entry.
     *
     * @param id        unique document ID
     * @param userId    owner user ID
     * @param tokenHash SHA-256 hash of the opaque token
     * @param familyId  token family identifier
     * @param expiresAt expiry instant
     */
    void save(String id, String userId, String tokenHash, String familyId, Instant expiresAt);

    /**
     * Finds a refresh token entry by its hash.
     */
    Optional<RefreshTokenEntry> findByTokenHash(String tokenHash);

    /**
     * Revokes a single token by its hash.
     */
    void revokeByTokenHash(String tokenHash);

    /**
     * Revokes all tokens belonging to the given family (replay attack mitigation).
     */
    void revokeAllByFamilyId(String familyId);

    /**
     * Immutable projection of a stored refresh token.
     */
    record RefreshTokenEntry(
            String id,
            String userId,
            String tokenHash,
            String familyId,
            Instant expiresAt,
            boolean isRevoked
    ) {}
}
