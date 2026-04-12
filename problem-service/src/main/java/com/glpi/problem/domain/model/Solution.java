package com.glpi.problem.domain.model;

import java.time.Instant;

/**
 * ITILSolution embedded in a problem.
 * Requirements: 10.6
 */
public class Solution {

    private String content;
    private String solutionType;
    private String authorId;
    private Instant createdAt;

    public Solution() {}

    public Solution(String content, String solutionType, String authorId, Instant createdAt) {
        this.content = content;
        this.solutionType = solutionType;
        this.authorId = authorId;
        this.createdAt = createdAt;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSolutionType() { return solutionType; }
    public void setSolutionType(String solutionType) { this.solutionType = solutionType; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
