package com.docheck.flow.infrastructure.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("system_health_events")
public class SystemHealthEventDocument {
    @Id
    private String id;

    @Indexed
    private String component;

    private String statusTag;

    @Indexed
    private String severity;

    private String message;
    private String details;
    private Map<String, Object> context;

    @Indexed
    private Instant occurredAt;

    private Instant createdAt;

    private boolean acknowledged;
    private String acknowledgedBy;
    private Instant acknowledgedAt;
    private String acknowledgementNotes;
}
