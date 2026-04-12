package com.glpi.notification.adapter.in.kafka;

import com.glpi.notification.domain.model.Actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared helper for extracting actors and context from Kafka event payloads.
 */
final class EventConsumerHelper {

    private EventConsumerHelper() {}

    @SuppressWarnings("unchecked")
    static List<Actor> extractActors(Map<String, Object> event) {
        Object payload = event.get("payload");
        if (payload instanceof Map) {
            Map<String, Object> payloadMap = (Map<String, Object>) payload;
            Object actorsObj = payloadMap.get("actors");
            if (actorsObj instanceof List) {
                List<Map<String, Object>> actorsList = (List<Map<String, Object>>) actorsObj;
                List<Actor> actors = new ArrayList<>();
                for (Map<String, Object> actorMap : actorsList) {
                    Actor actor = new Actor();
                    Object actorType = actorMap.get("actorType");
                    actor.setActorType(actorType instanceof Number ? ((Number) actorType).intValue() : 0);
                    actor.setActorKind(actorMap.getOrDefault("actorKind", "user").toString());
                    actor.setActorId(actorMap.getOrDefault("actorId", "").toString());
                    Object useNotif = actorMap.get("useNotification");
                    actor.setUseNotification(useNotif instanceof Boolean ? (Boolean) useNotif : true);
                    actors.add(actor);
                }
                return actors;
            }
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> extractContext(Map<String, Object> event) {
        Map<String, Object> context = new HashMap<>();
        context.put("aggregateId", event.get("aggregateId"));
        context.put("eventType", event.get("eventType"));
        Object payload = event.get("payload");
        if (payload instanceof Map) {
            Map<String, Object> payloadMap = (Map<String, Object>) payload;
            context.put("title", payloadMap.getOrDefault("title", ""));
            context.put("entityId", payloadMap.getOrDefault("entityId", ""));
        }
        return context;
    }
}
