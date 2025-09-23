package com.docheck.flow.api.dto;

import com.docheck.flow.domain.model.AiProvider;
import lombok.Data;

import java.util.List;

/**
 * DTO for sending AI provider data back to the client.
 * The API key is intentionally masked.
 */
@Data
public class AiProviderResponse {
    private String id;
    private String name;
    private boolean active;
    private String apiKey;
    private String baseUrl;
    private String selectedModel;
    private List<String> models;

    public static AiProviderResponse fromEntity(AiProvider entity) {
        AiProviderResponse dto = new AiProviderResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setActive(entity.isActive());
        dto.setBaseUrl(entity.getBaseUrl());
        dto.setSelectedModel(entity.getSelectedModel());
        dto.setModels(entity.getModels());

        // Mask the API key for security
        if (entity.getApiKey() != null && !entity.getApiKey().isEmpty()) {
            dto.setApiKey("********");
        } else {
            dto.setApiKey(null);
        }

        return dto;
    }
}
