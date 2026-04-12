package com.glpi.problem.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.problem.domain.model.Problem;
import com.glpi.problem.domain.model.ProblemNotFoundException;
import com.glpi.problem.domain.model.ProblemStatus;
import com.glpi.problem.domain.port.in.CloseProblemUseCase;
import com.glpi.problem.domain.port.out.EventPublisherPort;
import com.glpi.problem.domain.port.out.ProblemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing CloseProblemUseCase.
 * Requirements: 10.9
 */
@Service
public class CloseProblemService implements CloseProblemUseCase {

    private final ProblemRepository problemRepository;
    private final EventPublisherPort eventPublisher;
    private final StatusTransitionService statusTransitionService;

    public CloseProblemService(ProblemRepository problemRepository,
                               EventPublisherPort eventPublisher,
                               StatusTransitionService statusTransitionService) {
        this.problemRepository = problemRepository;
        this.eventPublisher = eventPublisher;
        this.statusTransitionService = statusTransitionService;
    }

    @Override
    public Problem closeProblem(String problemId) {
        Instant now = Instant.now();
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ProblemNotFoundException(problemId));

        statusTransitionService.validate(problem.getStatus(), ProblemStatus.CLOSED);
        problem.setStatus(ProblemStatus.CLOSED);
        problem.setClosedAt(now);
        problem.setUpdatedAt(now);

        Problem saved = problemRepository.save(problem);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "ProblemClosed",
                saved.getId(),
                "Problem",
                now,
                1,
                saved
        ));

        return saved;
    }
}
