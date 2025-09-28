package com.docheck.flow.application.service;

import com.docheck.flow.api.dto.ChatRequest;
import com.docheck.flow.api.dto.ChatResponse;
import com.docheck.flow.api.dto.AiProviderUpdateRequest;
import com.docheck.flow.application.service.provider.ChatProviderFactory;
import com.docheck.flow.application.service.security.EncryptionService;
import com.docheck.flow.domain.model.AiProvider;
// import com.docheck.flow.infrastructure.mongo.AiProviderRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AiProviderService {

    // private final AiProviderRepository aiProviderRepository;
    private final MongoTemplate mongoTemplate;
    private final ChatProviderFactory chatProviderFactory;
    private final EncryptionService encryptionService;
    private final WebClient.Builder webClientBuilder;

    public AiProvider updateProvider(String providerId, String providerName, AiProviderUpdateRequest request) {
        Query query = Query.query(Criteria.where("_id").is(providerId));
        Update update = new Update();

        // sempre garantir nome e id
        if (providerName != null) {
            update.set("name", providerName);
        }

        // campos opcionais enviados no request
        if (request.getBaseUrl() != null) {
            update.set("baseUrl", request.getBaseUrl());
        }
        if (request.getSelectedModel() != null) {
            update.set("selectedModel", request.getSelectedModel());
        }
        if (request.getModels() != null) {
            update.set("models", request.getModels());
        }

        // ativo: mantendo lógica original de sempre definir a partir do request
        update.set("active", request.isActive());

        // apiKey: só atualizar se vier e não estiver mascarada
        if (request.getApiKey() != null && !request.getApiKey().contains("*")) {
            String encryptedKey = encryptionService.encrypt(request.getApiKey());
            update.set("apiKey", encryptedKey);
        }

        // garante o _id para caso de upsert
        update.setOnInsert("_id", providerId);

        mongoTemplate.upsert(query, update, AiProvider.class);
        return mongoTemplate.findById(providerId, AiProvider.class);
    }

    public Optional<AiProvider> getProvider(String providerId) {
        return Optional.ofNullable(mongoTemplate.findById(providerId, AiProvider.class));
    }

    public List<AiProvider> getAllProviders() {
        return mongoTemplate.findAll(AiProvider.class);
    }

    public ChatResponse getChatResponse(ChatRequest request) {
        AiProvider provider = Optional.ofNullable(mongoTemplate.findById(request.getProvider(), AiProvider.class))
                .orElseThrow(() -> new IllegalArgumentException("AI Provider not found: " + request.getProvider()));

        return chatProviderFactory.getProvider(provider.getName().toLowerCase())
                .getChatResponse(request, provider);
    }

    public Mono<String> checkOllamaStatus() {
        AiProvider provider = Optional.ofNullable(mongoTemplate.findById("ollama", AiProvider.class))
                .orElseThrow(() -> new IllegalStateException("Ollama provider not configured."));

        return webClientBuilder.baseUrl(provider.getBaseUrl()).build()
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> "Ollama is running.")
                .onErrorResume(e -> Mono.just("Failed to connect to Ollama: " + e.getMessage()));
    }

    public Mono<List<String>> getOllamaModels() {
        AiProvider provider = Optional.ofNullable(mongoTemplate.findById("ollama", AiProvider.class))
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
