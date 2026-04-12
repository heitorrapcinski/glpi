package com.glpi.identity.domain.port.in;

/**
 * Command for authenticating a user with username and password.
 */
public record AuthenticateUserCommand(String username, String password) {}
