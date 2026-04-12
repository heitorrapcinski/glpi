package com.glpi.identity.config;

import com.glpi.identity.domain.model.AuthType;
import com.glpi.identity.domain.model.Email;
import com.glpi.identity.domain.model.Entity;
import com.glpi.identity.domain.model.EntityConfig;
import com.glpi.identity.domain.model.Profile;
import com.glpi.identity.domain.model.TicketStatusMatrix;
import com.glpi.identity.domain.model.User;
import com.glpi.identity.domain.port.out.EntityRepository;
import com.glpi.identity.domain.port.out.ProfileRepository;
import com.glpi.identity.domain.port.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Seeds default data (root entity, profiles, users) on startup when collections are empty.
 * Requirements: 29.1, 29.2, 29.3, 29.4, 29.13
 */
@Component
public class IdentitySeeder {

    private static final Logger log = LoggerFactory.getLogger(IdentitySeeder.class);
    private static final int BCRYPT_COST = 12;

    private final EntityRepository entityRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public IdentitySeeder(
            EntityRepository entityRepository,
            ProfileRepository profileRepository,
            UserRepository userRepository) {
        this.entityRepository = entityRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(BCRYPT_COST);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        seedRootEntity();
        seedProfiles();
        seedUsers();
    }

    private void seedRootEntity() {
        if (entityRepository.findById("0").isPresent()) {
            log.info("[Seeder] entities: root entity already exists, skipping");
            return;
        }

        Entity root = new Entity("0", "Root Entity", null, 1, "Root Entity", new EntityConfig());
        entityRepository.save(root);
        log.info("[Seeder] entities: inserted 1 document at {}", Instant.now());
    }

    private void seedProfiles() {
        if (!profileRepository.findAll().isEmpty()) {
            log.info("[Seeder] profiles: collection already has data, skipping");
            return;
        }

        // Standard CRUD rights: READ=1, UPDATE=2, CREATE=4, DELETE=8, PURGE=16 → full = 31
        Map<String, Integer> fullRights = Map.of(
                "ticket", 31, "problem", 31, "change", 31,
                "profile", 6, "user", 31, "entity", 31, "group", 31
        );
        Map<String, Integer> readOnlyRights = Map.of(
                "ticket", 1, "problem", 1, "change", 1,
                "profile", 1, "user", 1, "entity", 1, "group", 1
        );
        Map<String, Integer> selfServiceRights = Map.of(
                "ticket", 5  // READ + CREATE
        );
        Map<String, Integer> observerRights = Map.of(
                "ticket", 1, "problem", 1, "change", 1
        );
        Map<String, Integer> technicianRights = Map.of(
                "ticket", 31, "problem", 7, "change", 7,
                "user", 1, "entity", 1, "group", 1
        );
        Map<String, Integer> supervisorRights = Map.of(
                "ticket", 31, "problem", 31, "change", 31,
                "user", 7, "entity", 3, "group", 7
        );
        Map<String, Integer> hotlinerRights = Map.of(
                "ticket", 7  // READ + UPDATE + CREATE
        );

        TicketStatusMatrix defaultMatrix = new TicketStatusMatrix(
                List.of(new int[]{1, 2}, new int[]{2, 3}, new int[]{3, 4}, new int[]{4, 2}, new int[]{5, 6})
        );

        List<Profile> profiles = List.of(
                new Profile("profile-self-service", "Self-Service", "helpdesk", true, false,
                        selfServiceRights, defaultMatrix),
                new Profile("profile-observer", "Observer", "central", false, false,
                        observerRights, defaultMatrix),
                new Profile("profile-admin", "Admin", "central", false, false,
                        fullRights, defaultMatrix),
                new Profile("profile-super-admin", "Super-Admin", "central", false, false,
                        fullRights, defaultMatrix),
                new Profile("profile-hotliner", "Hotliner", "helpdesk", false, false,
                        hotlinerRights, defaultMatrix),
                new Profile("profile-technician", "Technician", "central", false, false,
                        technicianRights, defaultMatrix),
                new Profile("profile-supervisor", "Supervisor", "central", false, false,
                        supervisorRights, defaultMatrix),
                new Profile("profile-read-only", "Read-Only", "central", false, false,
                        readOnlyRights, defaultMatrix)
        );

        for (Profile profile : profiles) {
            profileRepository.save(profile);
        }
        log.info("[Seeder] profiles: inserted {} documents at {}", profiles.size(), Instant.now());
    }

    private void seedUsers() {
        if (!userRepository.findAll().isEmpty()) {
            log.info("[Seeder] users: collection already has data, skipping");
            return;
        }

        List<SeedUser> seedUsers = List.of(
                new SeedUser("user-glpi", "glpi", "glpi", "profile-super-admin"),
                new SeedUser("user-post-only", "post-only", "postonly", "profile-self-service"),
                new SeedUser("user-tech", "tech", "tech", "profile-technician"),
                new SeedUser("user-normal", "normal", "normal", "profile-observer")
        );

        for (SeedUser su : seedUsers) {
            String hash = passwordEncoder.encode(su.password());
            User user = new User(
                    su.id(),
                    su.username(),
                    hash,
                    AuthType.DB_GLPI,
                    null,
                    List.of(new Email(su.username() + "@glpi.local", true)),
                    "0",
                    su.profileId()
            );
            userRepository.save(user);
        }
        log.info("[Seeder] users: inserted {} documents at {}", seedUsers.size(), Instant.now());
    }

    private record SeedUser(String id, String username, String password, String profileId) {}
}
