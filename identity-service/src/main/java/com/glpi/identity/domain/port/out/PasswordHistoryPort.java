package com.glpi.identity.domain.port.out;

import java.util.List;

/**
 * Driven port: manages password history for users.
 */
public interface PasswordHistoryPort {

    /**
     * Returns the last N password hashes for the given user.
     */
    List<String> getLastN(String userId, int n);

    /**
     * Records a new password hash in the user's history.
     */
    void record(String userId, String hash);
}
