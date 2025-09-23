package com.docheck.flow.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Represents an AI Provider configuration stored in MongoDB.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_providers")
public class AiProvider {

    @Id
    private String id; // e.g., "openai", "gemini"

    private String name;

    private boolean active;

    private String apiKey; // This should be stored encrypted

    private String baseUrl;

    private String selectedModel;

    private List<String> models;
}
