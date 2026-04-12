package com.glpi.identity.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.identity.domain.model.*;
import com.glpi.identity.domain.port.in.AuthResponse;
import com.glpi.identity.domain.port.in.AuthenticateUserCommand;
import com.glpi.identity.domain.port.in.AuthenticateUserUseCase;
import com.glpi.identity.domain.port.out.EventPublisherPort;
import com.glpi.identity.domain.port.out.ProfileRepository;
import com.glpi.identity.domain.port.out.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Authenticates users with password + optional TOTP, enforces lockout policy,
 * and issues JWT access + refresh tokens on success.
 */
@Service
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);
    private static final int BCRYPT_COST = 12;
    private static final long ACCESS_TOKEN_EXPIRES_IN = 3600L;

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final EventPublisherPort eventPublisher;
    private final JwtTokenService jwtTokenService;
    private final TotpService totpService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthenticateUserService(UserRepository userRepository,
                                   ProfileRepository profileRepository,
                                   EventPublisherPort eventPublisher,
                                   JwtTokenService jwtTokenService,
                                   TotpService totpService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.eventPublisher = eventPublisher;
        this.jwtTokenService = jwtTokenService;
        this.totpService = totpService;
        this.passwordEncoder = new BCryptPasswordEncoder(BCRYPT_COST);
    }

    @Override
    public AuthResponse authenticate(AuthenticateUserCommand command) {
        User user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new UserNotFoundException(command.username()));

        if (!user.isActive()) {
            throw new AccountInactiveException();
        }

        if (user.isLocked()) {
            throw new AccountLockedException(user.getLockedUntil());
        }

        boolean credentialsValid = passwordEncoder.matches(command.password(), user.getPasswordHash());

        if (!credentialsValid) {
            handleFailedLogin(user);
            if (user.isLocked()) {
                throw new AccountLockedException(user.getLockedUntil());
            }
            throw new UserNotFoundException(command.username()); // generic error to avoid enumeration
        }

        // TOTP check
        boolean totpRequired = user.isTwoFactorEnabled() || isTotpEnforcedByProfile(user.getProfileId());
        if (totpRequired) {
            if (command.totpCode() == null) {
                throw new TotpRequiredException();
            }
            String secret = user.getTotpSecret();
            if (secret == null || !totpService.verifyCode(secret, command.totpCode())) {
                throw new TotpInvalidException();
            }
        }

        // Successful login — reset failed attempts
        user.resetFailedLoginAttempts();
        userRepository.save(user);

        // Resolve rights from profile
        Map<String, Integer> rights = profileRepository.findById(user.getProfileId())
                .map(Profile::getRights)
                .orElse(Map.of());

        String accessToken = jwtTokenService.issueAccessTokenWithRights(user, rights, null);
        String refreshToken = jwtTokenService.issueRefreshToken(user, null);

        return new AuthResponse(accessToken, refreshToken, ACCESS_TOKEN_EXPIRES_IN);
    }

    private boolean isTotpEnforcedByProfile(String profileId) {
        if (profileId == null) return false;
        return profileRepository.findById(profileId)
                .map(Profile::isTwoFactorEnforced)
                .orElse(false);
    }

    private void handleFailedLogin(User user) {
        user.incrementFailedLoginAttempts();

        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            Instant lockUntil = Instant.now().plus(LOCKOUT_DURATION);
            user.lockUntil(lockUntil);
            userRepository.save(user);
            publishAccountLockedEvent(user);
        } else {
            userRepository.save(user);
        }
    }

    private void publishAccountLockedEvent(User user) {
        DomainEventEnvelope event = new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "AccountLocked",
                user.getId(),
                "User",
                Instant.now(),
                1,
                Map.of(
                        "userId", user.getId(),
                        "username", user.getUsername(),
                        "lockedUntil", user.getLockedUntil().toString()
                )
        );
        eventPublisher.publish(event);
    }
}
