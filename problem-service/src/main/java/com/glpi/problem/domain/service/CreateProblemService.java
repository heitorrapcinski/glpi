package com.glpi.problem.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.problem.domain.model.Problem;
import com.glpi.problem.domain.model.ProblemStatus;
import com.glpi.problem.domain.port.in.CreateProblemCommand;
import com.glpi.problem.domain.port.in.CreateProblemUseCase;
import com.glpi.problem.domain.port.out.EventPublisherPort;
import com.glpi.problem.domain.port.out.ProblemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Domain service implementing CreateProblemUseCase.
 * Requirements: 10.2
 */
@Service
public class CreateProblemService implements CreateProblemUseCase {

    private final ProblemRepository problemRepository;
    private final EventPublisherPort eventPublisher;

    public CreateProblemService(ProblemRepository problemRepository, EventPublisherPort eventPublisher) {
        this.problemRepository = problemRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Problem createProblem(CreateProblemCommand cmd) {
        Instant now = Instant.now();

        Problem problem = new Problem();
        problem.setId(UUID.randomUUID().toString());
        problem.setStatus(ProblemStatus.INCOMING);
        problem.setTitle(cmd.title());
        problem.setContent(cmd.content());
        problem.setEntityId(cmd.entityId());
        problem.setUrgency(cmd.urgency());
        problem.setImpact(cmd.impact());
        problem.setPriority(cmd.priority());
        problem.setImpactContent(cmd.impactContent());
        problem.setCauseContent(cmd.causeContent());
        problem.setSymptomContent(cmd.symptomContent());
        problem.setActors(cmd.actors() != null ? cmd.actors() : new ArrayList<>());
        problem.setLinkedAssets(cmd.linkedAssets() != null ? cmd.linkedAssets() : new ArrayList<>());
        problem.setCreatedAt(now);
        problem.setUpdatedAt(now);

        Problem saved = problemRepository.save(problem);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "ProblemCreated",
                saved.getId(),
                "Problem",
                now,
                1,
                saved
        ));

        return saved;
    }
}
