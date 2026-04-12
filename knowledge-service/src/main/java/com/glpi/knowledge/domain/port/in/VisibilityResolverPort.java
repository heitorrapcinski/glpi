package com.glpi.knowledge.domain.port.in;

import com.glpi.knowledge.domain.model.KnowbaseItem;
import com.glpi.knowledge.domain.model.UserContext;

import java.util.List;

/**
 * Driving port — resolves article visibility based on user context.
 * Requirements: 17.3, 17.4, 17.5, 17.6
 */
public interface VisibilityResolverPort {

    boolean isVisibleTo(KnowbaseItem article, UserContext user);

    List<KnowbaseItem> filterVisible(List<KnowbaseItem> articles, UserContext user);
}
