package com.glpi.identity.domain.model;

/**
 * Thrown when TOTP verification is required but no code was provided.
 * Maps to HTTP 401 / TOTP_REQUIRED.
 */
public class TotpRequiredException extends RuntimeException {
    public TotpRequiredException() {
        super("TOTP verification code is required");
    }
}
