package com.glpi.problem.domain.service;

import com.glpi.problem.domain.model.Problem;
import com.glpi.problem.domain.model.ProblemNotFoundException;
import com.glpi.problem.domain.model.ProblemTask;
import com.glpi.problem.domain.port.in.AddTaskCommand;
import com.glpi.problem.domain.port.in.AddTaskUseCase;
import com.glpi.problem.domain.port.out.ProblemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing AddTaskUseCase.
 */
@Service
public class AddTaskService implements AddTaskUseCase {

    private final ProblemRepository problemRepository;

    public AddTaskService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Override
    public Problem addTask(AddTaskCommand cmd) {
        Instant now = Instant.now();
        Problem problem = problemRepository.findById(cmd.problemId())
                .orElseThrow(() -> new ProblemNotFoundException(cmd.problemId()));

        ProblemTask task = new ProblemTask(
                UUID.randomUUID().toString(),
                cmd.content(),
                cmd.assignedUserId(),
                cmd.status() > 0 ? cmd.status() : 1,
                cmd.isPrivate(),
                now
        );
        problem.getTasks().add(task);
        problem.setUpdatedAt(now);

        return problemRepository.save(problem);
    }
}
