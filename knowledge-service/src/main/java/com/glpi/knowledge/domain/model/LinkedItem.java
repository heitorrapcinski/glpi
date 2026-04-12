package com.glpi.knowledge.domain.model;

/**
 * Link between a KB article and another ITIL item (ticket, problem, change).
 * Requirements: 17.11
 */
public class LinkedItem {

    private String itemType;
    private String itemId;

    public LinkedItem() {}

    public LinkedItem(String itemType, String itemId) {
        this.itemType = itemType;
        this.itemId = itemId;
    }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
}
