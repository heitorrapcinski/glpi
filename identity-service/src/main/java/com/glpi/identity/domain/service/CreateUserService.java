package com.glpi.identity.domain.service;

import com.glpi.identity.domain.model.DuplicateUsernameException;
import com.glpi.identity.domain.model.PasswordComplexityException;
import com.glpi.identity.domain.model.PasswordHistoryException;
import com.glpi.identity.domain.model.User;
import com.glpi.identity.domain.port.in.CreateUserCommand;
import com.glpi.identity.domain.port.in.CreateUserUseCase;
import com.glpi.identity.domain.port.in.UserResponse;
import com.glpi.identity.domain.port.out.PasswordHistoryPort;
import com.glpi.identity.domain.port.out.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain service implementing CreateUserUseCase.
 * Enforces password complexity, duplicate rejection, and password history.
 */
@Service
public class CreateUserService implements CreateUserUseCase {

    private static final int BCRYPT_COST = 12;
    private static final int PASSWORD_HISTORY_DEPTH = 5;

    private final UserRepository userRepository;
    private final PasswordHistoryPort passwordHistoryPort;
    private final BCryptPasswordEncoder passwordEncoder;

    public CreateUserService(UserRepository userRepository, PasswordHistoryPort passwordHistoryPort) {
        this.userRepository = userRepository;
        this.passwordHistoryPort = passwordHistoryPort;
        this.passwordEncoder = new BCryptPasswordEncoder(BCRYPT_COST);
    }

    @Override
    public UserResponse createUser(CreateUserCommand command) {
        // 1. Validate password complexity
        validatePasswordComplexity(command.password());

        // 2. Reject duplicate username + authType
        if (userRepository.existsByUsernameAndAuthType(command.username(), command.authType())) {
            throw new DuplicateUsernameException(command.username());
        }

        // 3. Hash the password
        String passwordHash = passwordEncoder.encode(command.password());

        // 4. Build the User aggregate
        String userId = UUID.randomUUID().toString();
        User user = new User(
                userId,
                command.username(),
                passwordHash,
                command.authType(),
                null,
                command.emails(),
                command.entityId(),
                command.profileId()
        );

        // 5. Check password history (for new users there is no history, but we still check for consistency)
        List<String> history = passwordHistoryPort.getLastN(userId, PASSWORD_HISTORY_DEPTH);
        for (String previousHash : history) {
            if (passwordEncoder.matches(command.password(), previousHash)) {
                throw new PasswordHistoryException();
            }
        }

        // 6. Persist user
        User saved = userRepository.save(user);

        // 7. Record password in history
        passwordHistoryPort.record(saved.getId(), passwordHash);

        return toResponse(saved);
    }

    private void validatePasswordComplexity(String password) {
        List<String> violations = new ArrayList<>();

        if (password == null || password.length() < 8) {
            violations.add("Password must be at least 8 characters long");
        }
        if (password != null && !password.chars().anyMatch(Character::isUpperCase)) {
            violations.add("Password must contain at least one uppercase letter");
        }
        if (password != null && !password.chars().anyMatch(Character::isDigit)) {
            violations.add("Password must contain at least one digit");
        }

        if (!violations.isEmpty()) {
            throw new PasswordComplexityException(violations);
        }
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getAuthType(),
                user.getAuthSourceId(),
                user.getEmails(),
                user.isActive(),
                user.isDeleted(),
                user.getEntityId(),
                user.getProfileId(),
                user.getLanguage(),
                user.isTwoFactorEnabled(),
                user.getFailedLoginAttempts(),
                user.getLockedUntil(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
