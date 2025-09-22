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
@Document(collection = "ai_model_configs")
public class AiModelConfigDocument {
    @Id
    String id;

    @Indexed
    String providerId;

    @Indexed
    String modelName;

    String alias;
    Double temperature;
    Integer maxTokens;
    Boolean defaultModel;
    Boolean available;
    String description;
    Map<String, Object> extras;

    @Indexed
    Instant createdAt;
    Instant updatedAt;

    // Ollama specific fields
    Long size;
    String digest;
    String[] tags;
}