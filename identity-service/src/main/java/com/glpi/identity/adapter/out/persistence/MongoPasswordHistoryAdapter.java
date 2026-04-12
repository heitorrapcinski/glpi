package com.glpi.identity.adapter.out.persistence;

import com.glpi.identity.domain.port.out.PasswordHistoryPort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MongoDB adapter for the PasswordHistoryPort.
 */
@Component
public class MongoPasswordHistoryAdapter implements PasswordHistoryPort {

    private final MongoPasswordHistoryRepository repo;

    public MongoPasswordHistoryAdapter(MongoPasswordHistoryRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<String> getLastN(String userId, int n) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(n)
                .map(PasswordHistoryDocument::getPasswordHash)
                .toList();
    }

    @Override
    public void record(String userId, String hash) {
        repo.save(new PasswordHistoryDocument(userId, hash));
    }
}
