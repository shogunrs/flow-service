package com.docheck.flow.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

@Value
@Builder
public class AiCredential {
    String id;
    String providerId;
    String label;
    String encryptedApiKey;
    String maskedKey;
    String keyPreview;
    String organizationId;
    Instant createdAt;
    Instant updatedAt;
    Instant expiresAt;
    boolean enabled;
    Instant lastValidatedAt;
    Instant lastTestedAt;
    Boolean lastTestSuccess;
    String lastTestMessage;
    Map<String, Object> metadata;
}