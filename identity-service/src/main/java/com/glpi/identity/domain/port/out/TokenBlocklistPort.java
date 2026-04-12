package com.glpi.identity.domain.port.out;

import java.time.Duration;

/**
 * Driven port: JWT blocklist for logout support.
 * Tokens are stored by their JTI claim for the remainder of their validity.
 */
public interface TokenBlocklistPort {

    /**
     * Adds a JTI to the blocklist for the given TTL.
     *
     * @param jti the JWT ID claim
     * @param ttl how long to keep the entry (should equal remaining token validity)
     */
    void block(String jti, Duration ttl);

    /**
     * Returns true if the given JTI is currently blocklisted.
     */
    boolean isBlocked(String jti);
}
