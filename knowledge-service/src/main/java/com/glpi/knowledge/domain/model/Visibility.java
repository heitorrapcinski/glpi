package com.glpi.knowledge.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Visibility rules embedded in a KnowbaseItem.
 * Controls which users, groups, profiles, and entities can see the article.
 * Requirements: 17.3
 */
public class Visibility {

    private List<String> userIds = new ArrayList<>();
    private List<String> groupIds = new ArrayList<>();
    private List<String> profileIds = new ArrayList<>();
    private List<EntityRule> entityRules = new ArrayList<>();

    public Visibility() {}

    public List<String> getUserIds() { return userIds; }
    public void setUserIds(List<String> userIds) { this.userIds = userIds; }

    public List<String> getGroupIds() { return groupIds; }
    public void setGroupIds(List<String> groupIds) { this.groupIds = groupIds; }

    public List<String> getProfileIds() { return profileIds; }
    public void setProfileIds(List<String> profileIds) { this.profileIds = profileIds; }

    public List<EntityRule> getEntityRules() { return entityRules; }
    public void setEntityRules(List<EntityRule> entityRules) { this.entityRules = entityRules; }
}
