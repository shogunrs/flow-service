package com.docheck.flow.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class AiCredentialSecretResponse {
    private String providerId;
    private String credentialId;
    private String label;
    private String apiKey;
    private String organizationId;
    private Instant expiresAt;
    private Map<String, Object> metadata;
}