package com.glpi.identity.domain.port.out;

import com.glpi.identity.domain.model.AuthType;
import com.glpi.identity.domain.model.User;

import java.util.Optional;

/**
 * Driven port: persistence contract for the User aggregate.
 */
public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findById(String id);

    User save(User user);

    void delete(String id);

    boolean existsByUsernameAndAuthType(String username, AuthType authType);
}
