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
        String apiKey = entity.getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            String masked;
            if (apiKey.length() > 6) {
                // mostra 3 primeiros e 3 últimos
                masked = apiKey.substring(0, 3)
                        + "*".repeat(apiKey.length() - 6)
                        + apiKey.substring(apiKey.length() - 3);
            } else {
                // menor ou igual a 6 -> mostra só os 3 últimos
                masked = "*".repeat(Math.max(0, apiKey.length() - 3))
                        + apiKey.substring(Math.max(0, apiKey.length() - 3));
            }
            dto.setApiKey(masked);
        } else {
            dto.setApiKey(null);
        }

        return dto;
    }
}
