package com.docheck.flow.domain.model;

import java.time.Instant;

public class Process {
    private String id;
    private String externalId;
    private String name;
    private boolean active;
    private ProcessType type = ProcessType.GENERIC;
    private Instant createdAt;
    private Instant updatedAt;

    public Process() {}

    public Process(String id, String externalId, String name, boolean active, boolean isFinanceiro, Instant createdAt, Instant updatedAt) {
        this(id, externalId, name, active, isFinanceiro ? ProcessType.FINANCIAL : ProcessType.GENERIC, createdAt, updatedAt);
    }

    public Process(String id, String externalId, String name, boolean active, ProcessType type, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.active = active;
        this.type = type == null ? ProcessType.GENERIC : type;
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
    public boolean isFinanceiro() { return type == ProcessType.FINANCIAL; }
    public void setFinanceiro(boolean isFinanceiro) { this.type = isFinanceiro ? ProcessType.FINANCIAL : ProcessType.GENERIC; }
    public ProcessType getType() { return type; }
    public void setType(ProcessType type) { this.type = type == null ? ProcessType.GENERIC : type; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public enum ProcessType {
        GENERIC,
        FINANCIAL,
        TODO_LIST,
        LEAD_QUALIFICATION;

        public static ProcessType fromString(String value) {
            if (value == null || value.isBlank()) {
                return GENERIC;
            }
            try {
                return ProcessType.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return GENERIC;
            }
        }
    }
}
