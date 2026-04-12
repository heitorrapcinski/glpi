package com.glpi.identity.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.identity.domain.model.User;
import com.glpi.identity.domain.model.UserNotFoundException;
import com.glpi.identity.domain.port.in.PurgeUserUseCase;
import com.glpi.identity.domain.port.out.EventPublisherPort;
import com.glpi.identity.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Domain service implementing PurgeUserUseCase.
 * Hard-deletes the user document and publishes a UserPurged event.
 */
@Service
public class PurgeUserService implements PurgeUserUseCase {

    private final UserRepository userRepository;
    private final EventPublisherPort eventPublisher;

    public PurgeUserService(UserRepository userRepository, EventPublisherPort eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void purgeUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        userRepository.delete(userId);

        publishUserPurgedEvent(userId);
    }

    private void publishUserPurgedEvent(String userId) {
        Instant occurredAt = Instant.now();
        DomainEventEnvelope event = new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "UserPurged",
                userId,
                "User",
                occurredAt,
                1,
                Map.of(
                        "userId", userId,
                        "occurredAt", occurredAt.toString()
                )
        );
        eventPublisher.publish(event);
    }
}
