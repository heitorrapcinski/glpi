package com.glpi.identity.adapter.out.persistence;

import com.glpi.identity.domain.model.Profile;
import com.glpi.identity.domain.model.TicketStatusMatrix;
import com.glpi.identity.domain.port.out.ProfileRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter implementing the ProfileRepository driven port using MongoDB.
 */
@Component
public class ProfileRepositoryAdapter implements ProfileRepository {

    private final MongoProfileRepository mongoRepo;

    public ProfileRepositoryAdapter(MongoProfileRepository mongoRepo) {
        this.mongoRepo = mongoRepo;
    }

    @Override
    public Optional<Profile> findById(String id) {
        return mongoRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Profile save(Profile profile) {
        ProfileDocument doc = toDocument(profile);
        ProfileDocument saved = mongoRepo.save(doc);
        return toDomain(saved);
    }

    @Override
    public void delete(String id) {
        mongoRepo.deleteById(id);
    }

    @Override
    public List<Profile> findAll() {
        return mongoRepo.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Profile> findDefault() {
        return mongoRepo.findByIsDefaultTrue().map(this::toDomain);
    }

    @Override
    public long countProfilesWithUpdateRightOn(String resource) {
        return mongoRepo.findAll().stream()
                .map(this::toDomain)
                .filter(p -> p.hasUpdateRightOn(resource))
                .count();
    }

    // --- Mapping ---

    private Profile toDomain(ProfileDocument doc) {
        Map<String, Integer> rights = doc.getRights() != null ? new HashMap<>(doc.getRights()) : new HashMap<>();

        TicketStatusMatrix matrix = new TicketStatusMatrix();
        if (doc.getTicketStatusMatrix() != null && doc.getTicketStatusMatrix().getAllowed() != null) {
            matrix.setAllowed(doc.getTicketStatusMatrix().getAllowed());
        }

        Profile profile = new Profile(
                doc.getId(),
                doc.getName(),
                doc.getInterface(),
                doc.isDefault(),
                doc.isTwoFactorEnforced(),
                rights,
                matrix
        );
        profile.setCreatedAt(doc.getCreatedAt());
        profile.setUpdatedAt(doc.getUpdatedAt());
        return profile;
    }

    private ProfileDocument toDocument(Profile profile) {
        ProfileDocument doc = new ProfileDocument();
        doc.setId(profile.getId());
        doc.setName(profile.getName());
        doc.setInterface(profile.getInterface());
        doc.setDefault(profile.isDefault());
        doc.setTwoFactorEnforced(profile.isTwoFactorEnforced());
        doc.setRights(new HashMap<>(profile.getRights()));

        ProfileDocument.TicketStatusMatrixDocument matrixDoc = new ProfileDocument.TicketStatusMatrixDocument();
        matrixDoc.setAllowed(profile.getTicketStatusMatrix().getAllowed());
        doc.setTicketStatusMatrix(matrixDoc);

        doc.setCreatedAt(profile.getCreatedAt());
        doc.setUpdatedAt(profile.getUpdatedAt());
        return doc;
    }
}
