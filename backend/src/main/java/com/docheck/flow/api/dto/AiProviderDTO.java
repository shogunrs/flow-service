package com.docheck.flow.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class AiProviderDTO {
    private String id;
    private String name;
    private String slug;
    private String baseUrl;
    private String docsUrl;
    private String notes;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    // Statistics
    private long credentialCount;
    private long modelCount;
    private boolean hasActiveCredentials;
    private List<String> availableModels;
}