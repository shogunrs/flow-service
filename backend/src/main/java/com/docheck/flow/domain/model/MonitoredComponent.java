package com.docheck.flow.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder(toBuilder = true)
public class MonitoredComponent {
    private String id;
    private String name;
    private String componentKey;
    private String description;
    private String statusTag;
    private String defaultSeverity;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
