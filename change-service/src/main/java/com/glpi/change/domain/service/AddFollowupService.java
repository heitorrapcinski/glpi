package com.glpi.change.domain.service;

import com.glpi.change.domain.model.Change;
import com.glpi.change.domain.model.ChangeNotFoundException;
import com.glpi.change.domain.model.Followup;
import com.glpi.change.domain.port.in.AddFollowupCommand;
import com.glpi.change.domain.port.in.AddFollowupUseCase;
import com.glpi.change.domain.port.out.ChangeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain service implementing AddFollowupUseCase.
 * Requirements: 11.1
 */
@Service
public class AddFollowupService implements AddFollowupUseCase {

    private final ChangeRepository changeRepository;

    public AddFollowupService(ChangeRepository changeRepository) {
        this.changeRepository = changeRepository;
    }

    @Override
    public Change addFollowup(AddFollowupCommand cmd) {
        Instant now = Instant.now();
        Change change = changeRepository.findById(cmd.changeId())
                .orElseThrow(() -> new ChangeNotFoundException(cmd.changeId()));

        Followup followup = new Followup(
                UUID.randomUUID().toString(),
                cmd.content(),
                cmd.authorId(),
                cmd.isPrivate(),
                cmd.source(),
                now
        );
        change.getFollowups().add(followup);
        change.setUpdatedAt(now);

        return changeRepository.save(change);
    }
}
