package com.glpi.identity.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.identity.domain.model.AccountInactiveException;
import com.glpi.identity.domain.model.AccountLockedException;
import com.glpi.identity.domain.model.User;
import com.glpi.identity.domain.model.UserNotFoundException;
import com.glpi.identity.domain.port.in.AuthenticateUserCommand;
import com.glpi.identity.domain.port.out.EventPublisherPort;
import com.glpi.identity.domain.port.out.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Stub authentication service implementing account lockout logic.
 * Full JWT issuance is implemented in task 4.
 *
 * Lockout policy: after 5 consecutive failed logins within 10 minutes,
 * the account is locked for 15 minutes and an AccountLocked event is published.
 */
@Service
public class AuthenticateUserService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_WINDOW = Duration.ofMinutes(10);
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);
    private static final int BCRYPT_COST = 12;

    private final UserRepository userRepository;
    private final EventPublisherPort eventPublisher;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthenticateUserService(UserRepository userRepository, EventPublisherPort eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = new BCryptPasswordEncoder(BCRYPT_COST);
    }

    /**
     * Validates credentials and enforces lockout policy.
     * Returns the authenticated User on success.
     */
    public User authenticate(AuthenticateUserCommand command) {
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
            throw new AccountLockedException(user.getLockedUntil() != null
                    ? user.getLockedUntil()
                    : Instant.now());
        }

        // Successful login — reset failed attempts
        user.resetFailedLoginAttempts();
        userRepository.save(user);
        return user;
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
