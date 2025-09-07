package com.docheck.flow.domain.model;

import java.time.Instant;

public class Process {
    private String id;
    private String externalId;
    private String name;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public Process() {}

    public Process(String id, String externalId, String name, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
