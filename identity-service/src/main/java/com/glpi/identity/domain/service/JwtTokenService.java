package com.glpi.identity.domain.service;

import com.glpi.identity.domain.model.TokenExpiredException;
import com.glpi.identity.domain.model.User;
import com.glpi.identity.domain.port.out.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Domain service responsible for JWT RS256 issuance/validation and opaque refresh token management.
 */
@Service
public class JwtTokenService {

    private static final long ACCESS_TOKEN_HOURS = 1L;
    private static final long REFRESH_TOKEN_DAYS = 7L;

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtTokenService(PrivateKey jwtPrivateKey,
                           PublicKey jwtPublicKey,
                           RefreshTokenRepository refreshTokenRepository) {
        this.privateKey = jwtPrivateKey;
        this.publicKey = jwtPublicKey;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Issues a signed RS256 JWT access token for the given user.
     * Payload: sub, entity_id, profile_id, rights, iat, exp, jti.
     */
    public String issueAccessToken(User user) {
        return issueAccessToken(user, null);
    }

    /**
     * Issues a signed RS256 JWT access token, optionally recording an impersonating user.
     */
    public String issueAccessToken(User user, String impersonatedBy) {
        Instant now = Instant.now();
        Instant exp = now.plus(ACCESS_TOKEN_HOURS, ChronoUnit.HOURS);

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId())
                .claim("entity_id", user.getEntityId())
                .claim("profile_id", user.getProfileId())
                .claim("rights", user.getProfileId()) // rights resolved at runtime via profile
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(privateKey, Jwts.SIG.RS256);

        if (impersonatedBy != null) {
            builder.claim("impersonated_by", impersonatedBy);
        }

        return builder.compact();
    }

    /**
     * Issues an access token with explicit rights map embedded in the JWT.
     */
    public String issueAccessTokenWithRights(User user, Map<String, Integer> rights, String impersonatedBy) {
        Instant now = Instant.now();
        Instant exp = now.plus(ACCESS_TOKEN_HOURS, ChronoUnit.HOURS);

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId())
                .claim("entity_id", user.getEntityId())
                .claim("profile_id", user.getProfileId())
                .claim("rights", rights)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(privateKey, Jwts.SIG.RS256);

        if (impersonatedBy != null) {
            builder.claim("impersonated_by", impersonatedBy);
        }

        return builder.compact();
    }

    /**
     * Issues an opaque UUID refresh token, stores its hash in MongoDB.
     * Returns the plain token value (only time it is visible).
     *
     * @param user     the authenticated user
     * @param familyId token family ID (pass null to start a new family)
     */
    public String issueRefreshToken(User user, String familyId) {
        String plainToken = UUID.randomUUID().toString();
        String tokenHash = sha256(plainToken);
        String family = familyId != null ? familyId : UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS);

        refreshTokenRepository.save(
                UUID.randomUUID().toString(),
                user.getId(),
                tokenHash,
                family,
                expiresAt
        );

        return plainToken;
    }

    /**
     * Validates an RS256 JWT and returns its claims.
     *
     * @throws TokenExpiredException if the token has expired
     * @throws RuntimeException      if the token is invalid
     */
    public Claims validateAccessToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the JTI claim from a JWT without full validation (used for blocklisting).
     */
    public String extractJti(String token) {
        return validateAccessToken(token).getId();
    }

    /**
     * Computes the remaining validity duration in seconds for a JWT.
     */
    public long remainingValiditySeconds(String token) {
        Claims claims = validateAccessToken(token);
        long expEpoch = claims.getExpiration().toInstant().getEpochSecond();
        long nowEpoch = Instant.now().getEpochSecond();
        return Math.max(0, expEpoch - nowEpoch);
    }

    // --- Helpers ---

    static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 computation failed", e);
        }
    }
}
