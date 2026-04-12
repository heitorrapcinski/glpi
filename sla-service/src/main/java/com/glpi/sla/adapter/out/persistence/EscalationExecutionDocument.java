package com.glpi.sla.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Records each SLA escalation execution to prevent duplicate triggering.
 * Collection: escalation_executions
 * Requirements: 15.6
 */
@Document(collection = "escalation_executions")
public class EscalationExecutionDocument {

    @Id
    private String id;

    @Indexed
    private String ticketId;

    private String slaId;
    private String levelId;
    private Instant triggeredAt;

    public EscalationExecutionDocument() {}

    public EscalationExecutionDocument(String id, String ticketId, String slaId,
                                       String levelId, Instant triggeredAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.slaId = slaId;
        this.levelId = levelId;
        this.triggeredAt = triggeredAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getSlaId() { return slaId; }
    public void setSlaId(String slaId) { this.slaId = slaId; }

    public String getLevelId() { return levelId; }
    public void setLevelId(String levelId) { this.levelId = levelId; }

    public Instant getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(Instant triggeredAt) { this.triggeredAt = triggeredAt; }
}
