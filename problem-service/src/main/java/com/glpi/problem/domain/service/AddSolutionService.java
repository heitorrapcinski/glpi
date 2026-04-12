package com.glpi.problem.domain.service;

import com.glpi.common.DomainEventEnvelope;
import com.glpi.problem.domain.model.Problem;
import com.glpi.problem.domain.model.ProblemNotFoundException;
import com.glpi.problem.domain.model.ProblemStatus;
import com.glpi.problem.domain.model.Solution;
import com.glpi.problem.domain.port.out.EventPublisherPort;
import com.glpi.problem.domain.port.out.ProblemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service for adding a solution to a problem.
 */
@Service
public class AddSolutionService {

    private final ProblemRepository problemRepository;
    private final EventPublisherPort eventPublisher;
    private final StatusTransitionService statusTransitionService;

    public AddSolutionService(ProblemRepository problemRepository,
                              EventPublisherPort eventPublisher,
                              StatusTransitionService statusTransitionService) {
        this.problemRepository = problemRepository;
        this.eventPublisher = eventPublisher;
        this.statusTransitionService = statusTransitionService;
    }

    public Problem addSolution(String problemId, String content, String solutionType, String authorId) {
        Instant now = Instant.now();
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ProblemNotFoundException(problemId));

        statusTransitionService.validate(problem.getStatus(), ProblemStatus.SOLVED);
        problem.setSolution(new Solution(content, solutionType, authorId, now));
        problem.setStatus(ProblemStatus.SOLVED);
        problem.setSolvedAt(now);
        problem.setUpdatedAt(now);

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
