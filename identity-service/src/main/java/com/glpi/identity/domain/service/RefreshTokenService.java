package com.glpi.identity.domain.service;

import com.glpi.identity.domain.model.RefreshTokenReuseException;
import com.glpi.identity.domain.model.TokenExpiredException;
import com.glpi.identity.domain.model.UserNotFoundException;
import com.glpi.identity.domain.port.in.AuthResponse;
import com.glpi.identity.domain.port.in.RefreshTokenCommand;
import com.glpi.identity.domain.port.in.RefreshTokenUseCase;
import com.glpi.identity.domain.port.out.ProfileRepository;
import com.glpi.identity.domain.port.out.RefreshTokenRepository;
import com.glpi.identity.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Implements refresh token rotation with replay attack detection.
 * On replay: entire token family is invalidated and HTTP 401 is returned.
 */
@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    private static final long ACCESS_TOKEN_EXPIRES_IN = 3600L;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final JwtTokenService jwtTokenService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               ProfileRepository profileRepository,
                               JwtTokenService jwtTokenService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public AuthResponse refresh(RefreshTokenCommand command) {
        String tokenHash = JwtTokenService.sha256(command.refreshToken());

        RefreshTokenRepository.RefreshTokenEntry entry = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Replay attack: token already revoked → invalidate entire family
        if (entry.isRevoked()) {
            refreshTokenRepository.revokeAllByFamilyId(entry.familyId());
            throw new RefreshTokenReuseException();
        }

        // Check expiry
        if (Instant.now().isAfter(entry.expiresAt())) {
            refreshTokenRepository.revokeByTokenHash(tokenHash);
            throw new TokenExpiredException("Refresh token has expired");
        }

        // Revoke the used token
        refreshTokenRepository.revokeByTokenHash(tokenHash);

        // Load user
        var user = userRepository.findById(entry.userId())
                .orElseThrow(() -> new UserNotFoundException(entry.userId()));

        // Resolve rights from profile
        var rights = profileRepository.findById(user.getProfileId())
                .map(p -> p.getRights())
                .orElse(java.util.Map.of());

        // Issue new token pair (same family)
        String newAccessToken = jwtTokenService.issueAccessTokenWithRights(user, rights, null);
        String newRefreshToken = jwtTokenService.issueRefreshToken(user, entry.familyId());

        return new AuthResponse(newAccessToken, newRefreshToken, ACCESS_TOKEN_EXPIRES_IN);
    }
}
