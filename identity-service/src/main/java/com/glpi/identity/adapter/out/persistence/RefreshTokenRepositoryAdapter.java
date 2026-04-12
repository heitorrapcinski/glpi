package com.glpi.identity.adapter.out.persistence;

import com.glpi.identity.domain.port.out.RefreshTokenRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB adapter implementing the RefreshTokenRepository driven port.
 */
@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final MongoRefreshTokenRepository mongoRepo;

    public RefreshTokenRepositoryAdapter(MongoRefreshTokenRepository mongoRepo) {
        this.mongoRepo = mongoRepo;
    }

    @Override
    public void save(String id, String userId, String tokenHash, String familyId, Instant expiresAt) {
        RefreshTokenDocument doc = new RefreshTokenDocument();
        doc.setId(id);
        doc.setUserId(userId);
        doc.setTokenHash(tokenHash);
        doc.setFamilyId(familyId);
        doc.setExpiresAt(expiresAt);
        doc.setRevoked(false);
        doc.setCreatedAt(Instant.now());
        mongoRepo.save(doc);
    }

    @Override
    public Optional<RefreshTokenEntry> findByTokenHash(String tokenHash) {
        return mongoRepo.findByTokenHash(tokenHash).map(this::toEntry);
    }

    @Override
    public void revokeByTokenHash(String tokenHash) {
        mongoRepo.findByTokenHash(tokenHash).ifPresent(doc -> {
            doc.setRevoked(true);
            mongoRepo.save(doc);
        });
    }

    @Override
    public void revokeAllByFamilyId(String familyId) {
        List<RefreshTokenDocument> docs = mongoRepo.findAllByFamilyId(familyId);
        docs.forEach(doc -> doc.setRevoked(true));
        mongoRepo.saveAll(docs);
    }

    private RefreshTokenEntry toEntry(RefreshTokenDocument doc) {
        return new RefreshTokenEntry(
                doc.getId(),
                doc.getUserId(),
                doc.getTokenHash(),
                doc.getFamilyId(),
                doc.getExpiresAt(),
                doc.isRevoked()
        );
    }
}
