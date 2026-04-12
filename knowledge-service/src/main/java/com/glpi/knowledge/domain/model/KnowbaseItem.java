package com.glpi.knowledge.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * KnowbaseItem aggregate — a knowledge base article with visibility, revisions, comments, and links.
 * Requirements: 17.1, 17.3, 17.7, 17.8, 17.9, 17.11
 */
public class KnowbaseItem {

    private String id;
    private String title;
    private String answer;
    private String authorId;
    private boolean isFaq;
    private long viewCount;
    private Visibility visibility;
    private List<String> categoryIds = new ArrayList<>();
    private List<KnowbaseItemRevision> revisions = new ArrayList<>();
    private List<LinkedItem> linkedItems = new ArrayList<>();
    private List<KnowbaseItemComment> comments = new ArrayList<>();
    private Instant beginDate;
    private Instant endDate;
    private Instant createdAt;
    private Instant updatedAt;

    public KnowbaseItem() {
        this.visibility = new Visibility();
        this.viewCount = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public boolean isFaq() { return isFaq; }
    public void setFaq(boolean faq) { isFaq = faq; }

    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }

    public List<String> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<String> categoryIds) { this.categoryIds = categoryIds; }

    public List<KnowbaseItemRevision> getRevisions() { return revisions; }
    public void setRevisions(List<KnowbaseItemRevision> revisions) { this.revisions = revisions; }

    public List<LinkedItem> getLinkedItems() { return linkedItems; }
    public void setLinkedItems(List<LinkedItem> linkedItems) { this.linkedItems = linkedItems; }

    public List<KnowbaseItemComment> getComments() { return comments; }
    public void setComments(List<KnowbaseItemComment> comments) { this.comments = comments; }

    public Instant getBeginDate() { return beginDate; }
    public void setBeginDate(Instant beginDate) { this.beginDate = beginDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
