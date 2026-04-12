package com.glpi.identity.domain.model;

/**
 * Authentication type supported by the Identity Service.
 * Values match the legacy GLPI authtype constants.
 */
public enum AuthType {

    DB_GLPI(1),
    LDAP(2),
    OAUTH2(4);

    private final int value;

    AuthType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AuthType fromValue(int value) {
        for (AuthType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AuthType value: " + value);
    }
}
