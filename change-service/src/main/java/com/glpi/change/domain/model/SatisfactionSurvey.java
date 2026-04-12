package com.glpi.change.domain.model;

import java.time.Instant;

/**
 * Satisfaction survey embedded in a change.
 * Requirements: 11.10
 */
public class SatisfactionSurvey {

    private int satisfaction;
    private String comment;
    private Instant answeredAt;

    public SatisfactionSurvey() {}

    public int getSatisfaction() { return satisfaction; }
    public void setSatisfaction(int satisfaction) { this.satisfaction = satisfaction; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Instant getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(Instant answeredAt) { this.answeredAt = answeredAt; }
}
