package com.glpi.identity.domain.port.in;

/**
 * Driving port: use case for creating a new user.
 */
public interface CreateUserUseCase {

    UserResponse createUser(CreateUserCommand command);
}
