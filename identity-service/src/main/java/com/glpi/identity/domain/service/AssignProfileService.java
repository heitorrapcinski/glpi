package com.glpi.identity.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.identity.domain.model.EntityNotFoundException;
import com.glpi.identity.domain.model.ProfileNotFoundException;
import com.glpi.identity.domain.model.User;
import com.glpi.identity.domain.model.UserNotFoundException;
import com.glpi.identity.domain.port.in.AssignProfileCommand;
import com.glpi.identity.domain.port.in.AssignProfileUseCase;
import com.glpi.identity.domain.port.out.EntityRepository;
import com.glpi.identity.domain.port.out.EventPublisherPort;
import com.glpi.identity.domain.port.out.ProfileRepository;
import com.glpi.identity.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Domain service implementing AssignProfileUseCase.
 * Assigns a profile to a user in an entity and publishes a ProfileAssigned event.
 */
@Service
public class AssignProfileService implements AssignProfileUseCase {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final EntityRepository entityRepository;
    private final EventPublisherPort eventPublisher;

    public AssignProfileService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            EntityRepository entityRepository,
            EventPublisherPort eventPublisher) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.entityRepository = entityRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void assignProfile(AssignProfileCommand command) {
        // Validate all referenced aggregates exist
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        profileRepository.findById(command.profileId())
                .orElseThrow(() -> new ProfileNotFoundException(command.profileId()));

        entityRepository.findById(command.entityId())
                .orElseThrow(() -> new EntityNotFoundException(command.entityId()));

        // Update user's profile and entity assignment
        user.setProfileId(command.profileId());
        user.setEntityId(command.entityId());
        userRepository.save(user);

        // Publish ProfileAssigned domain event
        DomainEventEnvelope event = new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "ProfileAssigned",
                command.userId(),
                "Profile",
                Instant.now(),
                1,
                Map.of(
                        "userId", command.userId(),
                        "profileId", command.profileId(),
                        "entityId", command.entityId()
                )
        );
        eventPublisher.publish(event);
    }
}
