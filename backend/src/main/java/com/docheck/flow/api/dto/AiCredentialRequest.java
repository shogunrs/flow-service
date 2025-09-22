package com.docheck.flow.api.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class AiCredentialRequest {
    private String label;
    private String apiKey;
    private String organizationId;
    private Instant expiresAt;
    private Boolean enabled;
    private Map<String, Object> metadata;
}