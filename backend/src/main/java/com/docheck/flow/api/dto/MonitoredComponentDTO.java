package com.docheck.flow.api.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class MonitoredComponentDTO {
    String id;
    String name;
    String componentKey;
    String description;
    String statusTag;
    String defaultSeverity;
    boolean enabled;
    Instant createdAt;
    Instant updatedAt;
}
