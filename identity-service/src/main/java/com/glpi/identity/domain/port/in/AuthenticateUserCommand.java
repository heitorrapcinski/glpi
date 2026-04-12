package com.glpi.identity.domain.port.in;

/**
 * Command for authenticating a user with username, password, and optional TOTP code.
 */
public record AuthenticateUserCommand(String username, String password, Integer totpCode) {

    /** Convenience constructor without TOTP (for non-2FA users). */
    public AuthenticateUserCommand(String username, String password) {
        this(username, password, null);
    }
}
