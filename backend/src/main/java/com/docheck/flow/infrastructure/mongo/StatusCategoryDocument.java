package com.docheck.flow.infrastructure.mongo;

import com.docheck.flow.domain.model.StatusCategory;
import com.docheck.flow.domain.model.StatusItem;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "status_categories")
public class StatusCategoryDocument {
    @Id
    private String id;
    private String name;
    private String description;
    private String icon;
    private List<StatusItem> statuses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StatusCategoryDocument() {}

    public StatusCategoryDocument(StatusCategory category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.icon = category.getIcon();
        this.statuses = category.getStatuses();
        this.createdAt = category.getCreatedAt();
        this.updatedAt = category.getUpdatedAt();
    }

    public StatusCategory toDomain() {
        StatusCategory category = new StatusCategory();
        category.setId(this.id);
        category.setName(this.name);
        category.setDescription(this.description);
        category.setIcon(this.icon);
        category.setStatuses(this.statuses);
        category.setCreatedAt(this.createdAt);
        category.setUpdatedAt(this.updatedAt);
        return category;
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