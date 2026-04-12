package com.glpi.change.domain.service;

import com.glpi.change.domain.model.Change;
import com.glpi.change.domain.model.ChangeNotFoundException;
import com.glpi.change.domain.model.ValidationStep;
import com.glpi.change.domain.port.in.ApproveChangeValidationCommand;
import com.glpi.change.domain.port.in.ApproveChangeValidationUseCase;
import com.glpi.change.domain.port.out.ChangeRepository;
import com.glpi.change.domain.port.out.EventPublisherPort;
import com.glpi.common.DomainEventEnvelope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing ApproveChangeValidationUseCase.
 * Requirements: 11.5
 */
@Service
public class ApproveChangeValidationService implements ApproveChangeValidationUseCase {

    private final ChangeRepository changeRepository;
    private final EventPublisherPort eventPublisher;

    public ApproveChangeValidationService(ChangeRepository changeRepository, EventPublisherPort eventPublisher) {
        this.changeRepository = changeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Change approveValidation(ApproveChangeValidationCommand cmd) {
        Instant now = Instant.now();
        Change change = changeRepository.findById(cmd.changeId())
                .orElseThrow(() -> new ChangeNotFoundException(cmd.changeId()));

        change.getValidationSteps().stream()
                .filter(v -> v.getId().equals(cmd.validationId()))
                .findFirst()
                .ifPresent(v -> {
                    v.setStatus(2); // ACCEPTED
                    v.setComment(cmd.comment());
                });

        change.setUpdatedAt(now);
        Change saved = changeRepository.save(change);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "ChangeValidationApproved",
                saved.getId(),
                "Change",
                now,
                1,
                saved
        ));

        return saved;
    }
}
