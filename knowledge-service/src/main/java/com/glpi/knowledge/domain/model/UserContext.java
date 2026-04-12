package com.glpi.knowledge.domain.model;

import java.util.List;

/**
 * User context extracted from JWT headers for visibility resolution.
 * Requirements: 17.3, 17.4, 17.5, 17.6
 */
public class UserContext {

    private String userId;
    private String entityId;
    private String profileId;
    private String profileInterface; // "central", "helpdesk", or null for anonymous
    private List<String> groupIds;

    public UserContext() {}

    public UserContext(String userId, String entityId, String profileId,
                       String profileInterface, List<String> groupIds) {
        this.userId = userId;
        this.entityId = entityId;
        this.profileId = profileId;
        this.profileInterface = profileInterface;
        this.groupIds = groupIds;
    }

    public boolean isAnonymous() { return userId == null || userId.isBlank(); }
    public boolean isHelpdesk() { return "helpdesk".equals(profileInterface); }
    public boolean isCentral() { return "central".equals(profileInterface); }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }

    public String getProfileInterface() { return profileInterface; }
    public void setProfileInterface(String profileInterface) { this.profileInterface = profileInterface; }

    public List<String> getGroupIds() { return groupIds; }
    public void setGroupIds(List<String> groupIds) { this.groupIds = groupIds; }
}
