package com.glpi.notification.domain.port.in;

import com.glpi.notification.domain.model.Actor;

import java.util.List;
import java.util.Map;

/**
 * Driving port — dispatches notifications for a domain event.
 * Requirements: 16.1, 16.5
 */
public interface NotificationDispatchUseCase {

    /**
     * Dispatch notifications for the given event type and actors.
     *
     * @param eventType the event type (e.g. "ticket.created")
     * @param actors    the actors on the ITIL object
     * @param context   template rendering context (key-value pairs)
     */
    void dispatch(String eventType, List<Actor> actors, Map<String, Object> context);
}
