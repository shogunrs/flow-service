package com.docheck.flow.application.service;

import com.docheck.flow.api.dto.ChatRequest;
import com.docheck.flow.api.dto.ChatResponse;
import com.docheck.flow.api.dto.AiProviderUpdateRequest;
import com.docheck.flow.domain.model.AiProvider;
import com.docheck.flow.infrastructure.mongo.AiProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiProviderService {

    private final AiProviderRepository aiProviderRepository;

    public AiProvider updateProvider(String providerId, String providerName, AiProviderUpdateRequest request) {
        // Find the existing provider or create a new one if it doesn't exist
        AiProvider provider = aiProviderRepository.findById(providerId).orElse(new AiProvider());

        provider.setId(providerId);
        provider.setName(providerName); // Set the name
        provider.setActive(request.isActive());
        provider.setBaseUrl(request.getBaseUrl());
        provider.setSelectedModel(request.getSelectedModel());
        provider.setModels(request.getModels());

        // IMPORTANT: Only update the API key if a new, non-masked key is provided.
        if (request.getApiKey() != null && !request.getApiKey().contains("*")) {
            // WARNING: Implement a real encryption service here.
            String encryptedKey = "encrypted(" + request.getApiKey() + ")"; // Placeholder for encryption
            provider.setApiKey(encryptedKey);
        }

        return aiProviderRepository.save(provider);
    }

    public Optional<AiProvider> getProvider(String providerId) {
        return aiProviderRepository.findById(providerId);
    }

    public List<AiProvider> getAllProviders() {
        return aiProviderRepository.findAll();
    }

    /**
     * Example method to call the provider's chat endpoint.
     * This is a simplified placeholder — each provider would need its own
     * implementation.
     */
    public ChatResponse getChatResponse(ChatRequest request) {
        // 1. Retrieve the AiProvider configuration from the database
        Optional<AiProvider> optionalProvider = aiProviderRepository.findById(request.getProvider());

        if (optionalProvider.isEmpty()) {
            throw new IllegalArgumentException("AI Provider not found: " + request.getProvider());
        }

        AiProvider provider = optionalProvider.get();

        // 2. Extract necessary information
        String apiKey = provider.getApiKey();
        // WARNING: Implement a real decryption service here if API keys are encrypted.
        if (apiKey != null && apiKey.startsWith("encrypted(")) {
            // Placeholder for decryption
            apiKey = apiKey.substring("encrypted(".length(), apiKey.length() - 1);
        }
        final String finalApiKey = apiKey;

        String baseUrl = provider.getBaseUrl();
        String model = request.getModel(); // Use the model selected in the chat playground
        String prompt = request.getPrompt();

        // 3. Build the request body for the AI API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        requestBody.put("temperature", 0.7);

        // 4. Determine the full URL for the chat completion endpoint
        String fullUrl = baseUrl;
        if (!fullUrl.endsWith("/")) {
            fullUrl += "/";
        }
        fullUrl += "v1/chat/completions"; // Common path for OpenAI-like APIs

        // TODO: Use WebClient or RestTemplate to POST requestBody with finalApiKey as
        // header.
        // This is just a placeholder response for now.
        ChatResponse response = new ChatResponse();
        response.setContent("Mocked response from " + fullUrl + " with model " + model);
        return response;
    }
}