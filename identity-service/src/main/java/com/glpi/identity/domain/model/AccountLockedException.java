package com.glpi.identity.domain.model;

import java.time.Instant;

/**
 * Thrown when authentication is attempted for a locked account. Maps to HTTP 401.
 */
public class AccountLockedException extends RuntimeException {

    private final Instant lockedUntil;

    public AccountLockedException(Instant lockedUntil) {
        super("Account is locked until " + lockedUntil);
        this.lockedUntil = lockedUntil;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }
}
