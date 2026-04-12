package com.glpi.identity.adapter.out.persistence;

import com.glpi.identity.domain.model.AuthType;
import com.glpi.identity.domain.model.Email;
import com.glpi.identity.domain.model.User;
import com.glpi.identity.domain.port.out.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing the UserRepository driven port using MongoDB.
 */
@Component
public class UserRepositoryAdapter implements UserRepository {

    private final MongoUserRepository mongoRepo;

    public UserRepositoryAdapter(MongoUserRepository mongoRepo) {
        this.mongoRepo = mongoRepo;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return mongoRepo.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(String id) {
        return mongoRepo.findById(id).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserDocument doc = toDocument(user);
        UserDocument saved = mongoRepo.save(doc);
        return toDomain(saved);
    }

    @Override
    public void delete(String id) {
        mongoRepo.deleteById(id);
    }

    @Override
    public boolean existsByUsernameAndAuthType(String username, AuthType authType) {
        return mongoRepo.existsByUsernameAndAuthType(username, authType);
    }

    @Override
    public List<User> findAll() {
        return mongoRepo.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    // --- Mapping ---

    private User toDomain(UserDocument doc) {
        List<Email> emails = doc.getEmails() == null ? List.of() :
                doc.getEmails().stream()
                        .map(e -> new Email(e.getEmail(), e.isDefault()))
                        .toList();

        User user = new User(
                doc.getId(),
                doc.getUsername(),
                doc.getPasswordHash(),
                doc.getAuthType(),
                doc.getAuthSourceId(),
                emails,
                doc.getEntityId(),
                doc.getProfileId()
        );

        user.setActive(doc.isActive());
        user.setDeleted(doc.isDeleted());
        user.setLanguage(doc.getLanguage() != null ? doc.getLanguage() : "en_US");
        user.setPersonalToken(doc.getPersonalToken());
        user.setApiToken(doc.getApiToken());
        user.setTotpSecret(doc.getTotpSecret());
        user.setTwoFactorEnabled(doc.isTwoFactorEnabled());
        user.setPasswordHistory(doc.getPasswordHistory() != null ? doc.getPasswordHistory() : List.of());
        user.setFailedLoginAttempts(doc.getFailedLoginAttempts());
        user.setLockedUntil(doc.getLockedUntil());
        user.setCreatedAt(doc.getCreatedAt());
        user.setUpdatedAt(doc.getUpdatedAt());

        return user;
    }

    private UserDocument toDocument(User user) {
        UserDocument doc = new UserDocument();
        doc.setId(user.getId());
        doc.setUsername(user.getUsername());
        doc.setPasswordHash(user.getPasswordHash());
        doc.setAuthType(user.getAuthType());
        doc.setAuthSourceId(user.getAuthSourceId());

        List<UserDocument.EmailDocument> emailDocs = user.getEmails().stream()
                .map(e -> new UserDocument.EmailDocument(e.email(), e.isDefault()))
                .toList();
        doc.setEmails(emailDocs);

        doc.setActive(user.isActive());
        doc.setDeleted(user.isDeleted());
        doc.setEntityId(user.getEntityId());
        doc.setProfileId(user.getProfileId());
        doc.setLanguage(user.getLanguage());
        doc.setPersonalToken(user.getPersonalToken());
        doc.setApiToken(user.getApiToken());
        doc.setTotpSecret(user.getTotpSecret());
        doc.setTwoFactorEnabled(user.isTwoFactorEnabled());
        doc.setPasswordHistory(user.getPasswordHistory());
        doc.setFailedLoginAttempts(user.getFailedLoginAttempts());
        doc.setLockedUntil(user.getLockedUntil());
        doc.setCreatedAt(user.getCreatedAt());
        doc.setUpdatedAt(user.getUpdatedAt());

        return doc;
    }
}
