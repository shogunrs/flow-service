package com.docheck.flow.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class AiCredentialDTO {
    private String id;
    private String providerId;
    private String label;
    private String maskedKey;
    private String keyPreview;
    private String organizationId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;
    private boolean enabled;
    private Instant lastValidatedAt;
    private Instant lastTestedAt;
    private Boolean lastTestSuccess;
    private String lastTestMessage;
    private Map<String, Object> metadata;
}