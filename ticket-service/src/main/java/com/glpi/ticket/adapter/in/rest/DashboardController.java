package com.glpi.ticket.adapter.in.rest;

import com.glpi.ticket.domain.model.ActorType;
import com.glpi.ticket.domain.model.TicketStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller that serves aggregated dashboard statistics.
 * Mounted at /dashboard so the API gateway can route /dashboard/** here.
 */
@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Dashboard statistics")
public class DashboardController {

    private final MongoTemplate mongoTemplate;

    public DashboardController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics")
    public DashboardStatsResponse getStats(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        Criteria notDeleted = Criteria.where("isDeleted").is(false);

        // --- Counters ---
        long openTickets = mongoTemplate.count(
                Query.query(notDeleted.and("status").in(
                        TicketStatus.INCOMING, TicketStatus.ASSIGNED, TicketStatus.PLANNED)),
                "tickets");

        long myAssignedTickets = 0;
        if (userId != null && !userId.isBlank()) {
            myAssignedTickets = mongoTemplate.count(
                    Query.query(Criteria.where("isDeleted").is(false)
                            .and("status").nin(TicketStatus.CLOSED)
                            .and("actors").elemMatch(
                                    Criteria.where("actorType").is(ActorType.ASSIGNED)
                                            .and("actorId").is(userId))),
                    "tickets");
        }

        long overdueTickets = mongoTemplate.count(
                Query.query(Criteria.where("isDeleted").is(false)
                        .and("status").nin(TicketStatus.SOLVED, TicketStatus.CLOSED)
                        .and("sla.ttrDeadline").lt(Instant.now())),
                "tickets");

        long pendingTickets = mongoTemplate.count(
                Query.query(Criteria.where("isDeleted").is(false)
                        .and("status").is(TicketStatus.WAITING)),
                "tickets");

        var counters = new DashboardCounters(openTickets, myAssignedTickets, overdueTickets, pendingTickets);

        // --- Tickets by status ---
        List<StatusChartEntry> ticketsByStatus = Arrays.stream(TicketStatus.values())
                .map(s -> {
                    long count = mongoTemplate.count(
                            Query.query(Criteria.where("isDeleted").is(false).and("status").is(s)),
                            "tickets");
                    return new StatusChartEntry(s.getValue(), s.name(), count);
                })
                .collect(Collectors.toList());

        // --- Tickets by priority (1-6) ---
        List<PriorityChartEntry> ticketsByPriority = new ArrayList<>();
        String[] priorityLabels = {"", "Very Low", "Low", "Medium", "High", "Very High", "Major"};
        for (int p = 1; p <= 6; p++) {
            long count = mongoTemplate.count(
                    Query.query(Criteria.where("isDeleted").is(false).and("priority").is(p)),
                    "tickets");
            String label = p < priorityLabels.length ? priorityLabels[p] : String.valueOf(p);
            ticketsByPriority.add(new PriorityChartEntry(p, label, count));
        }

        // --- Recent activity (last 20 tickets updated) ---
        Query recentQuery = Query.query(Criteria.where("isDeleted").is(false))
                .with(Sort.by(Sort.Direction.DESC, "updatedAt"))
                .limit(20);
        recentQuery.fields().include("id", "title", "status", "updatedAt", "actors");

        List<ActivityFeedEntry> recentActivity = mongoTemplate.find(recentQuery,
                        org.bson.Document.class, "tickets").stream()
                .map(doc -> {
                    String ticketId = doc.get("_id") != null ? doc.get("_id").toString() : "";
                    String title = doc.getString("title");
                    String status = doc.get("status") != null ? doc.get("status").toString() : "";
                    Instant updatedAt = doc.get("updatedAt") != null
                            ? ((Date) doc.get("updatedAt")).toInstant() : Instant.now();
                    // Try to extract the requester name from actors
                    String authorName = "System";
                    @SuppressWarnings("unchecked")
                    List<org.bson.Document> actors = doc.getList("actors", org.bson.Document.class);
                    if (actors != null) {
                        authorName = actors.stream()
                                .filter(a -> "REQUESTER".equals(a.getString("actorType")))
                                .map(a -> a.getString("actorId"))
                                .findFirst().orElse("System");
                    }
                    return new ActivityFeedEntry(ticketId, "update", "Ticket",
                            ticketId, title != null ? title : "", authorName,
                            updatedAt.toString());
                })
                .collect(Collectors.toList());

        return new DashboardStatsResponse(counters, ticketsByStatus, ticketsByPriority, recentActivity);
    }

    // --- DTOs ---

    public record DashboardStatsResponse(
            DashboardCounters counters,
            List<StatusChartEntry> ticketsByStatus,
            List<PriorityChartEntry> ticketsByPriority,
            List<ActivityFeedEntry> recentActivity) {}

    public record DashboardCounters(
            long openTickets,
            long myAssignedTickets,
            long overdueTickets,
            long pendingTickets) {}

    public record StatusChartEntry(int status, String label, long count) {}

    public record PriorityChartEntry(int priority, String label, long count) {}

    public record ActivityFeedEntry(
            String id, String type, String objectType,
            String objectId, String objectTitle,
            String authorName, String createdAt) {}
}
