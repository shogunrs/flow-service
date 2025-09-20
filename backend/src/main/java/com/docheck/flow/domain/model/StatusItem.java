package com.docheck.flow.domain.model;

import java.time.LocalDateTime;

public class StatusItem {
    private String id;
    private String name;
    private String description;
    private String color;
    private String categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StatusItem() {}

    public StatusItem(String id, String name, String description, String color, String categoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.categoryId = categoryId;
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

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}