package com.glpi.knowledge.domain.service;

import com.glpi.knowledge.domain.model.*;
import com.glpi.knowledge.domain.port.in.VisibilityResolverPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves article visibility based on user context.
 * - Anonymous: only isFaq=true with root entity (id=0) recursive visibility
 * - Helpdesk: only isFaq=true matching user's entity/group/profile
 * - Central: all articles matching user's entity/group/profile
 * Requirements: 17.3, 17.4, 17.5, 17.6
 */
@Service
public class VisibilityResolverService implements VisibilityResolverPort {

    private static final String ROOT_ENTITY_ID = "0";

    @Override
    public boolean isVisibleTo(KnowbaseItem article, UserContext user) {
        if (!isWithinDateWindow(article)) {
            return false;
        }

        if (user.isAnonymous()) {
            return article.isFaq() && hasRootEntityRecursiveVisibility(article);
        }

        if (user.isHelpdesk()) {
            return article.isFaq() && matchesUserVisibility(article, user);
        }

        // Central users see all articles matching visibility rules
        return matchesUserVisibility(article, user);
    }

    @Override
    public List<KnowbaseItem> filterVisible(List<KnowbaseItem> articles, UserContext user) {
        return articles.stream()
                .filter(article -> isVisibleTo(article, user))
                .collect(Collectors.toList());
    }

    private boolean isWithinDateWindow(KnowbaseItem article) {
        Instant now = Instant.now();
        if (article.getBeginDate() != null && now.isBefore(article.getBeginDate())) {
            return false;
        }
        if (article.getEndDate() != null && now.isAfter(article.getEndDate())) {
            return false;
        }
        return true;
    }

    private boolean hasRootEntityRecursiveVisibility(KnowbaseItem article) {
        Visibility vis = article.getVisibility();
        if (vis == null || vis.getEntityRules() == null) {
            return false;
        }
        return vis.getEntityRules().stream()
                .anyMatch(rule -> ROOT_ENTITY_ID.equals(rule.getEntityId()) && rule.isRecursive());
    }

    private boolean matchesUserVisibility(KnowbaseItem article, UserContext user) {
        Visibility vis = article.getVisibility();
        if (vis == null) {
            return false;
        }

        // Check user-level visibility
        if (vis.getUserIds() != null && vis.getUserIds().contains(user.getUserId())) {
            return true;
        }

        // Check group-level visibility
        if (vis.getGroupIds() != null && user.getGroupIds() != null) {
            for (String groupId : user.getGroupIds()) {
                if (vis.getGroupIds().contains(groupId)) {
                    return true;
                }
            }
        }

        // Check profile-level visibility
        if (vis.getProfileIds() != null && vis.getProfileIds().contains(user.getProfileId())) {
            return true;
        }

        // Check entity-level visibility
        if (vis.getEntityRules() != null) {
            for (EntityRule rule : vis.getEntityRules()) {
                if (rule.getEntityId().equals(user.getEntityId())) {
                    return true;
                }
                if (rule.isRecursive() && ROOT_ENTITY_ID.equals(rule.getEntityId())) {
                    return true;
                }
            }
        }

        return false;
    }
}
