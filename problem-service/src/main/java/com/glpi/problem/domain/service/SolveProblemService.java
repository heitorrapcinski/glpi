package com.glpi.problem.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.problem.domain.model.Problem;
import com.glpi.problem.domain.model.ProblemNotFoundException;
import com.glpi.problem.domain.model.ProblemStatus;
import com.glpi.problem.domain.model.Solution;
import com.glpi.problem.domain.port.in.SolveProblemCommand;
import com.glpi.problem.domain.port.in.SolveProblemUseCase;
import com.glpi.problem.domain.port.out.EventPublisherPort;
import com.glpi.problem.domain.port.out.ProblemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing SolveProblemUseCase.
 * Requirements: 10.6
 */
@Service
public class SolveProblemService implements SolveProblemUseCase {

    private final ProblemRepository problemRepository;
    private final EventPublisherPort eventPublisher;
    private final StatusTransitionService statusTransitionService;

    public SolveProblemService(ProblemRepository problemRepository,
                               EventPublisherPort eventPublisher,
                               StatusTransitionService statusTransitionService) {
        this.problemRepository = problemRepository;
        this.eventPublisher = eventPublisher;
        this.statusTransitionService = statusTransitionService;
    }

    @Override
    public Problem solveProblem(SolveProblemCommand cmd) {
        Instant now = Instant.now();
        Problem problem = problemRepository.findById(cmd.problemId())
                .orElseThrow(() -> new ProblemNotFoundException(cmd.problemId()));

        statusTransitionService.validate(problem.getStatus(), ProblemStatus.SOLVED);
        problem.setStatus(ProblemStatus.SOLVED);
        problem.setSolvedAt(now);
        problem.setUpdatedAt(now);

        if (cmd.solutionContent() != null) {
            problem.setSolution(new Solution(
                    cmd.solutionContent(), cmd.solutionType(), cmd.authorId(), now));
        }

        Problem saved = problemRepository.save(problem);

        eventPublisher.publish(new DomainEventEnvelope(
                UUID.randomUUID().toString(),
                "ProblemSolved",
                saved.getId(),
                "Problem",
                now,
                1,
                saved
        ));

        return saved;
    }
}
