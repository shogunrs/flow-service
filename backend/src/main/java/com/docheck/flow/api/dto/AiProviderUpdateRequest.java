package com.docheck.flow.api.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO for receiving AI provider update requests from the API.
 */
@Data
public class AiProviderUpdateRequest {
    private String apiKey;
    private String baseUrl;
    private String selectedModel;
    private List<String> models;
    private boolean active;
}
