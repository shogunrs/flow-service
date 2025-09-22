package com.docheck.flow.infrastructure.mongo;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.Map;

@Value
@Builder(toBuilder = true)
@Document(collection = "ai_credentials")
public class AiCredentialDocument {
    @Id
    String id;

    @Indexed
    String providerId;

    @Indexed
    String label;

    String encryptedApiKey;
    String maskedKey;
    String keyPreview;
    String organizationId;

    @Indexed
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