package com.docheck.flow.domain.model;

import java.time.Instant;

public class Process {
    private String id;
    private String key;
    private String name;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public Process() {}

    public Process(String id, String key, String name, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

