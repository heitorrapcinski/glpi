package com.glpi.identity.domain.model;

/**
 * Thrown when the provided TOTP code is invalid.
 * Maps to HTTP 401 / TOTP_INVALID.
 */
public class TotpInvalidException extends RuntimeException {
    public TotpInvalidException() {
        super("Invalid TOTP verification code");
    }
}
