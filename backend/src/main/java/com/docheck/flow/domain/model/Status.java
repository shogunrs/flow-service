package com.docheck.flow.domain.model;

import java.time.Instant;

public class Status {
    private String id;
    private String name;
    private String color;
    private String category; // ESTEIRA, USUARIOS, SISTEMA, etc.
    private Instant createdAt;
    private Instant updatedAt;

    public Status() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Status(String name, String color) {
        this();
        this.name = name;
        this.color = color;
        this.category = "ESTEIRA"; // Default category
    }

    public Status(String name, String color, String category) {
        this();
        this.name = name;
        this.color = color;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        this.updatedAt = Instant.now();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}