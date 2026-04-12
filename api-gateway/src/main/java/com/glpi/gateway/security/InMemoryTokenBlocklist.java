package com.glpi.gateway.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the JWT blocklist.
 * Entries expire automatically based on the TTL provided at block time.
 * For production, replace with a Redis-backed implementation.
 */
@Component
public class InMemoryTokenBlocklist {

    private final ConcurrentHashMap<String, Instant> blocklist = new ConcurrentHashMap<>();

    /**
     * Adds a JTI to the blocklist for the given TTL.
     */
    public void block(String jti, Duration ttl) {
        blocklist.put(jti, Instant.now().plus(ttl));
    }

    /**
     * Returns true if the given JTI is currently blocklisted and not yet expired.
     */
    public boolean isBlocked(String jti) {
        Instant expiry = blocklist.get(jti);
        if (expiry == null) {
            return false;
        }
        if (Instant.now().isAfter(expiry)) {
            blocklist.remove(jti);
            return false;
        }
        return true;
    }
}
