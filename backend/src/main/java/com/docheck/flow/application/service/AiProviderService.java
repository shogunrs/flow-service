package com.docheck.flow.application.service;

import com.docheck.flow.api.dto.ChatRequest;
import com.docheck.flow.api.dto.ChatResponse;
import com.docheck.flow.api.dto.AiProviderUpdateRequest;
import com.docheck.flow.application.service.provider.ChatProviderFactory;
import com.docheck.flow.application.service.security.EncryptionService;
import com.docheck.flow.domain.model.AiProvider;
import com.docheck.flow.infrastructure.mongo.AiProviderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AiProviderService {

    private final AiProviderRepository aiProviderRepository;
    private final ChatProviderFactory chatProviderFactory;
    private final EncryptionService encryptionService;
    private final WebClient.Builder webClientBuilder;

    public AiProvider updateProvider(String providerId, String providerName, AiProviderUpdateRequest request) {
        AiProvider provider = aiProviderRepository.findById(providerId).orElse(new AiProvider());

        provider.setId(providerId);
        provider.setName(providerName);
        provider.setActive(request.isActive());
        provider.setBaseUrl(request.getBaseUrl());
        provider.setSelectedModel(request.getSelectedModel());
        provider.setModels(request.getModels());

        if (request.getApiKey() != null && !request.getApiKey().contains("*")) {
            String encryptedKey = encryptionService.encrypt(request.getApiKey());
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

    public ChatResponse getChatResponse(ChatRequest request) {
        AiProvider provider = aiProviderRepository.findById(request.getProvider())
            .orElseThrow(() -> new IllegalArgumentException("AI Provider not found: " + request.getProvider()));

        return chatProviderFactory.getProvider(provider.getName().toLowerCase())
            .getChatResponse(request, provider);
    }

    public Mono<String> checkOllamaStatus() {
        AiProvider provider = aiProviderRepository.findById("ollama")
            .orElseThrow(() -> new IllegalStateException("Ollama provider not configured."));

        return webClientBuilder.baseUrl(provider.getBaseUrl()).build()
            .get()
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> "Ollama is running.")
            .onErrorResume(e -> Mono.just("Failed to connect to Ollama: " + e.getMessage()));
    }

    public Mono<List<String>> getOllamaModels() {
        AiProvider provider = aiProviderRepository.findById("ollama")
            .orElseThrow(() -> new IllegalStateException("Ollama provider not configured."));

        String url = provider.getBaseUrl();
        if (!url.endsWith("/")) {
            url += "/";
        }
        url += "api/tags";

        return webClientBuilder.build()
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .map(this::parseOllamaModelsResponse);
    }

    private List<String> parseOllamaModelsResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode modelsNode = root.path("models");
            return StreamSupport.stream(modelsNode.spliterator(), false)
                .map(modelNode -> modelNode.path("name").asText())
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Ollama models response", e);
        }
    }
}
