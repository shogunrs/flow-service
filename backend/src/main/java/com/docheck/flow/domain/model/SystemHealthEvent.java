package com.docheck.flow.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder(toBuilder = true)
public class SystemHealthEvent {
    private String id;
    private String component;
    private String statusTag;
    private String severity;
    private String message;
    private String details;
    private Map<String, Object> context;
    private Instant occurredAt;
    private Instant createdAt;
    private boolean acknowledged;
    private String acknowledgedBy;
    private Instant acknowledgedAt;
    private String acknowledgementNotes;
}
