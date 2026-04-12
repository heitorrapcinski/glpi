package com.glpi.identity.adapter.out.persistence;

import com.glpi.identity.domain.port.out.TokenBlocklistPort;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of TokenBlocklistPort for development.
 * Uses a ConcurrentHashMap with expiry timestamps.
 * For production, replace with a Redis-backed implementation.
 */
@Component
public class InMemoryTokenBlocklist implements TokenBlocklistPort {

    private final ConcurrentHashMap<String, Instant> blocklist = new ConcurrentHashMap<>();

    @Override
    public void block(String jti, Duration ttl) {
        Instant expiry = Instant.now().plus(ttl);
        blocklist.put(jti, expiry);
    }

    @Override
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
