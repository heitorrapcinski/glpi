package com.glpi.identity.domain.service;

import com.glpi.identity.domain.port.in.LogoutUseCase;
import com.glpi.identity.domain.port.out.TokenBlocklistPort;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Implements logout by extracting the JWT JTI and adding it to the blocklist
 * for the remainder of the token's validity period.
 */
@Service
public class LogoutService implements LogoutUseCase {

    private final JwtTokenService jwtTokenService;
    private final TokenBlocklistPort tokenBlocklist;

    public LogoutService(JwtTokenService jwtTokenService, TokenBlocklistPort tokenBlocklist) {
        this.jwtTokenService = jwtTokenService;
        this.tokenBlocklist = tokenBlocklist;
    }

    @Override
    public void logout(String accessToken) {
        Claims claims = jwtTokenService.validateAccessToken(accessToken);
        String jti = claims.getId();
        Instant exp = claims.getExpiration().toInstant();
        Duration remaining = Duration.between(Instant.now(), exp);
        if (!remaining.isNegative()) {
            tokenBlocklist.block(jti, remaining);
        }
    }
}
