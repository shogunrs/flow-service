package com.docheck.flow.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class AiModelConfigDTO {
    private String id;
    private String providerId;
    private String modelName;
    private String alias;
    private Double temperature;
    private Integer maxTokens;
    private boolean defaultModel;
    private boolean available;
    private String description;
    private Map<String, Object> extras;
    private Instant createdAt;
    private Instant updatedAt;

    // Ollama specific
    private Long size;
    private String digest;
    private String[] tags;
}