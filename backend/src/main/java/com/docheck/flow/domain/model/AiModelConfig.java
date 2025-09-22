package com.docheck.flow.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

@Value
@Builder
public class AiModelConfig {
    String id;
    String providerId;
    String modelName;
    String alias;
    Double temperature;
    Integer maxTokens;
    boolean defaultModel;
    boolean available;
    String description;
    Map<String, Object> extras;
    Instant createdAt;
    Instant updatedAt;

    // Ollama specific fields
    Long size;
    String digest;
    String[] tags;
}