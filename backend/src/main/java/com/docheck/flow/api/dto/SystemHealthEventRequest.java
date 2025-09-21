package com.docheck.flow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthEventRequest {
    private String component;
    private String statusTag;
    private String severity;
    private String message;
    private String details;
    private Map<String, Object> context;
    private Instant occurredAt;
}
