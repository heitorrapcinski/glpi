package com.glpi.notification.domain.service;

import com.glpi.notification.domain.model.Actor;
import com.glpi.notification.domain.model.NotificationChannel;
import com.glpi.notification.domain.model.NotificationTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Resolves notification recipients from ITIL object actors.
 * Skips actors with useNotification=false.
 * Requirements: 16.5, 16.10
 */
@Service
public class NotificationTargetResolver {

    private static final Logger log = LoggerFactory.getLogger(NotificationTargetResolver.class);

    /**
     * Resolve notification targets from actors.
     * Filters out actors with useNotification=false.
     * For user actors, creates an email target using the actor ID as a placeholder address.
     *
     * @param actors  the actors on the ITIL object
     * @param context optional context containing additional recipient info
     * @return list of resolved notification targets
     */
    public List<NotificationTarget> resolve(List<Actor> actors, Map<String, Object> context) {
        List<NotificationTarget> targets = new ArrayList<>();

        if (actors == null || actors.isEmpty()) {
            return targets;
        }

        for (Actor actor : actors) {
            if (!actor.isUseNotification()) {
                log.debug("Skipping actor {} — notifications disabled", actor.getActorId());
                continue;
            }

            // Resolve email address from context or use a placeholder
            String email = resolveEmail(actor, context);
            if (email != null && !email.isBlank()) {
                targets.add(new NotificationTarget(
                        actor.getActorId(),
                        email,
                        NotificationChannel.EMAIL
                ));
            }
        }

        return targets;
    }

    private String resolveEmail(Actor actor, Map<String, Object> context) {
        // Check if context provides email mapping for this actor
        if (context != null && context.containsKey("emails")) {
            @SuppressWarnings("unchecked")
            Map<String, String> emails = (Map<String, String>) context.get("emails");
            if (emails != null && emails.containsKey(actor.getActorId())) {
                return emails.get(actor.getActorId());
            }
        }
        // Fallback: use actorId@glpi.local as placeholder
        return actor.getActorId() + "@glpi.local";
    }
}
