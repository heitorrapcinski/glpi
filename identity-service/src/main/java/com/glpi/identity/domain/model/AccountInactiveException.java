package com.glpi.identity.domain.model;

/**
 * Thrown when authentication is attempted for a deactivated user. Maps to HTTP 401.
 */
public class AccountInactiveException extends RuntimeException {

    public AccountInactiveException() {
        super("Account is inactive");
    }
}
