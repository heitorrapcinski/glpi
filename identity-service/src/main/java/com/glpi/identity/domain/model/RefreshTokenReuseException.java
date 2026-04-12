package com.glpi.identity.domain.model;

/**
 * Thrown when a refresh token is used more than once (replay attack detected).
 * The entire token family is invalidated. Maps to HTTP 401 / REFRESH_TOKEN_REUSE.
 */
public class RefreshTokenReuseException extends RuntimeException {
    public RefreshTokenReuseException() {
        super("Refresh token reuse detected — entire token family has been invalidated");
    }
}
