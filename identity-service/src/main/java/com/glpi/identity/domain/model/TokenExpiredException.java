package com.glpi.identity.domain.model;

/**
 * Thrown when a JWT or refresh token has expired.
 * Maps to HTTP 401 / TOKEN_EXPIRED.
 */
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException() {
        super("Token has expired");
    }

    public TokenExpiredException(String message) {
        super(message);
    }
}
