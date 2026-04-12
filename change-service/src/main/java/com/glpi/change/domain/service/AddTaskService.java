package com.glpi.change.domain.service;

import com.glpi.change.domain.model.Change;
import com.glpi.change.domain.model.ChangeNotFoundException;
import com.glpi.change.domain.model.ChangeTask;
import com.glpi.change.domain.port.in.AddTaskCommand;
import com.glpi.change.domain.port.in.AddTaskUseCase;
import com.glpi.change.domain.port.out.ChangeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing AddTaskUseCase.
 * Requirements: 11.1
 */
@Service
public class AddTaskService implements AddTaskUseCase {

    private final ChangeRepository changeRepository;

    public AddTaskService(ChangeRepository changeRepository) {
        this.changeRepository = changeRepository;
    }

    @Override
    public Change addTask(AddTaskCommand cmd) {
        Instant now = Instant.now();
        Change change = changeRepository.findById(cmd.changeId())
                .orElseThrow(() -> new ChangeNotFoundException(cmd.changeId()));

        ChangeTask task = new ChangeTask(
                UUID.randomUUID().toString(),
                cmd.content(),
                cmd.assignedUserId(),
                cmd.status(),
                cmd.isPrivate(),
                now
        );
        change.getTasks().add(task);
        change.setUpdatedAt(now);

        return changeRepository.save(change);
    }
}
