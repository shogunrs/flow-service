package com.docheck.flow.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class StatusCategory {
    private String id;
    private String name;
    private String description;
    private String icon;
    private List<StatusItem> statuses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StatusCategory() {}

    public StatusCategory(String id, String name, String description, String icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public List<StatusItem> getStatuses() { return statuses; }
    public void setStatuses(List<StatusItem> statuses) { this.statuses = statuses; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}