package com.docheck.flow.application.service;

import com.docheck.flow.api.dto.*;
import com.docheck.flow.api.dto.AiTestResult;
import com.docheck.flow.infrastructure.mongo.AiCredentialDocument;
import com.docheck.flow.infrastructure.mongo.AiCredentialRepository;
import com.docheck.flow.infrastructure.mongo.AiModelConfigRepository;
import com.docheck.flow.infrastructure.mongo.AiModelConfigDocument;
import com.docheck.flow.infrastructure.mongo.AiProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.docheck.flow.infrastructure.mongo.AiProviderDocument;

import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiProviderService {

    private final AiProviderRepository providerRepository;
    private final AiCredentialRepository credentialRepository;
    private final AiModelConfigRepository modelRepository;
    private final AiCredentialCryptoService cryptoService;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<AiProviderDTO> listProviders() {
        return providerRepository.findAll()
                .stream()
                .map(this::toProviderDTO)
                .collect(Collectors.toList());
    }

    public AiProviderDTO createProvider(AiProviderRequest request) {
        validateProviderRequest(request);

        Instant now = Instant.now();
        AiProviderDocument document = AiProviderDocument.builder()
                .name(request.getName())
                .slug(generateSlug(request.getName(), request.getSlug()))
                .baseUrl(request.getBaseUrl())
                .docsUrl(request.getDocsUrl())
                .notes(request.getNotes())
                .enabled(Optional.ofNullable(request.getEnabled()).orElse(true))
                .createdAt(now)
                .updatedAt(now)
                .build();

        AiProviderDocument saved = providerRepository.save(document);
        log.info("AI Provider created: {}", saved.getName());

        // Auto-detect models if it's Ollama
        if (isOllamaProvider(saved)) {
            autoDetectOllamaModels(saved.getId());
        }

        return toProviderDTO(saved);
    }

    public AiProviderDTO updateProvider(String providerId, AiProviderRequest request) {
        AiProviderDocument existing = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider não encontrado"));

        validateProviderRequest(request);

        AiProviderDocument updated = existing.toBuilder()
                .name(request.getName())
                .slug(generateSlug(request.getName(), request.getSlug()))
                .baseUrl(request.getBaseUrl())
                .docsUrl(request.getDocsUrl())
                .notes(request.getNotes())
                .enabled(Optional.ofNullable(request.getEnabled()).orElse(existing.isEnabled()))
                .updatedAt(Instant.now())
                .build();

        AiProviderDocument saved = providerRepository.save(updated);
        log.info("AI Provider updated: {}", saved.getName());

        return toProviderDTO(saved);
    }

    public void deleteProvider(String providerId) {
        AiProviderDocument provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider não encontrado"));

        // Delete related credentials and models
        credentialRepository.deleteAll(credentialRepository.findByProviderId(providerId));
        modelRepository.deleteAll(modelRepository.findByProviderId(providerId));
        providerRepository.delete(provider);

        log.info("AI Provider deleted: {}", provider.getName());
    }

    // Auto-detection methods
    public List<AiModelConfigDTO> autoDetectModels(String providerId) {
        AiProviderDocument provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider não encontrado"));

        if (isOllamaProvider(provider)) {
            return autoDetectOllamaModels(providerId);
        }

        // Add other provider auto-detection here
        log.warn("Auto-detection not supported for provider: {}", provider.getName());
        return Collections.emptyList();
    }

    private List<AiModelConfigDTO> autoDetectOllamaModels(String providerId) {
        try {
            AiProviderDocument provider = providerRepository.findById(providerId).orElseThrow();
            String ollamaUrl = provider.getBaseUrl() != null ? provider.getBaseUrl() : "http://localhost:11434";

            // Call Ollama API to list models
            ResponseEntity<Map> response = restTemplate.getForEntity(ollamaUrl + "/api/tags", Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> models = (List<Map<String, Object>>) response.getBody().get("models");
                List<AiModelConfigDTO> newModels = new ArrayList<>();

                if (models != null) {
                    // Buscar modelos já existentes para evitar duplicatas
                    Set<String> existingModelNames = modelRepository.findByProviderId(providerId)
                            .stream()
                            .map(AiModelConfigDocument::getModelName)
                            .collect(Collectors.toSet());

                    for (Map<String, Object> modelData : models) {
                        String modelName = (String) modelData.get("name");
                        if (!existingModelNames.contains(modelName)) {
                            newModels.add(createOrUpdateOllamaModel(providerId, modelData));
                        }
                    }
                }
                return newModels;
            }
        } catch (Exception e) {
            log.error("Failed to auto-detect Ollama models for provider {}: {}", providerId, e.getMessage());
        }
        return Collections.emptyList();
    }

    private AiModelConfigDTO createOrUpdateOllamaModel(String providerId, Map<String, Object> modelData) {
        String modelName = (String) modelData.get("name");
        Long size = modelData.get("size") instanceof Number ? ((Number) modelData.get("size")).longValue() : null;
        String digest = (String) modelData.get("digest");

        Optional<AiModelConfigDocument> existing = modelRepository.findByProviderIdAndModelName(providerId, modelName);

        Instant now = Instant.now();
        AiModelConfigDocument document;

        if (existing.isPresent()) {
            document = existing.get().toBuilder()
                    .available(true)
                    .size(size)
                    .digest(digest)
                    .updatedAt(now)
                    .build();
            log.info("Ollama model auto-updated: {} for provider {}", modelName, providerId);
        } else {
            document = AiModelConfigDocument.builder()
                    .providerId(providerId)
                    .modelName(modelName)
                    .alias(generateModelAlias(modelName))
                    .available(true)
                    .defaultModel(false)
                    .description("Auto-detected Ollama model")
                    .size(size)
                    .digest(digest)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            log.info("Ollama model auto-detected and created: {} for provider {}", modelName, providerId);
        }

        AiModelConfigDocument saved = modelRepository.save(document);

        return toModelDTO(saved);
    }

    private boolean isOllamaProvider(AiProviderDocument provider) {
        return provider.getName().toLowerCase().contains("ollama") ||
                provider.getSlug().toLowerCase().contains("ollama") ||
                (provider.getBaseUrl() != null && provider.getBaseUrl().contains(":11434"));
    }

    private String generateSlug(String name, String providedSlug) {
        if (providedSlug != null && !providedSlug.trim().isEmpty()) {
            return providedSlug.toLowerCase().replaceAll("[^a-z0-9-]", "-");
        }
        return name.toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-");
    }

    private String generateModelAlias(String modelName) {
        // Extract base name before colon (remove tags like :latest)
        return modelName.split(":")[0];
    }

    private void validateProviderRequest(AiProviderRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do provider é obrigatório");
        }
    }

    public Map<String, Object> quickSetupOllama() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        // Check if Ollama provider already exists, or create it
        Optional<AiProviderDocument> existingProvider = providerRepository.findBySlug("ollama");

        AiProviderDocument ollamaProvider;
        if (existingProvider.isPresent()) {
            ollamaProvider = existingProvider.get();
            result.put("alreadyExists", true);
            log.info("Ollama provider already exists. Proceeding to detect models.");
        } else {
            AiProviderRequest request = new AiProviderRequest();
            request.setName("Ollama");
            request.setSlug("ollama");
            request.setBaseUrl("http://localhost:11434");
            request.setDocsUrl("https://ollama.ai/docs");
            request.setNotes(
                    "Ollama fornece acesso local a modelos de IA como Llama 3, Mistral, e outros. Execução totalmente local e gratuita.");
            request.setEnabled(true);

            result.put("alreadyExists", false);
            ollamaProvider = toDocument(createProvider(request));
            log.info("Ollama provider created.");
        }

        // Always try to auto-detect models
        List<AiModelConfigDTO> detectedModels = autoDetectOllamaModels(ollamaProvider.getId());

        data.put("provider", toProviderDTO(ollamaProvider));
        data.put("modelsDetected", detectedModels.size());
        result.put("data", data);
        result.put("message", String.format("Configuração do Ollama concluída. %d modelos detectados/atualizados.",
                detectedModels.size()));

        return result;
    }

    private void validateCredentialRequest(AiCredentialRequest request) {
        if (request.getLabel() == null || request.getLabel().trim().isEmpty()) {
            throw new IllegalArgumentException("Label da credential é obrigatório");
        }
        if (request.getApiKey() == null || request.getApiKey().trim().isEmpty()) {
            throw new IllegalArgumentException("API Key é obrigatória");
        }
    }

    private AiProviderDTO toProviderDTO(AiProviderDocument document) {
        long credentialCount = credentialRepository.countByProviderId(document.getId());
        long modelCount = modelRepository.countByProviderId(document.getId());
        boolean hasActiveCredentials = credentialRepository.countByProviderIdAndEnabledTrue(document.getId()) > 0;

        return AiProviderDTO.builder()
                .id(document.getId())
                .name(document.getName())
                .slug(document.getSlug())
                .baseUrl(document.getBaseUrl())
                .docsUrl(document.getDocsUrl())
                .notes(document.getNotes())
                .enabled(document.isEnabled())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .credentialCount(credentialCount)
                .modelCount(modelCount)
                .hasActiveCredentials(hasActiveCredentials)
                .build();
    }

    private AiProviderDocument toDocument(AiProviderDTO dto) {
        return AiProviderDocument.builder()
                .id(dto.getId())
                .name(dto.getName())
                .slug(dto.getSlug())
                .baseUrl(dto.getBaseUrl())
                .docsUrl(dto.getDocsUrl())
                .notes(dto.getNotes())
                .enabled(dto.isEnabled())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private AiModelConfigDTO toModelDTO(AiModelConfigDocument document) {
        return AiModelConfigDTO.builder()
                .id(document.getId())
                .providerId(document.getProviderId())
                .modelName(document.getModelName())
                .alias(document.getAlias())
                .temperature(document.getTemperature())
                .maxTokens(document.getMaxTokens()) // Assumes maxTokens is Integer
                .defaultModel(Optional.ofNullable(document.getDefaultModel()).orElse(false))
                .available(Optional.ofNullable(document.getAvailable()).orElse(false))
                .description(document.getDescription())
                .extras(document.getExtras())
                .createdAt(document.getCreatedAt()) // Assumes createdAt is Instant
                .updatedAt(document.getUpdatedAt())
                .size(document.getSize())
                .digest(document.getDigest())
                .tags(document.getTags())
                .build();
    }

    private AiCredentialDTO toCredentialDTO(AiCredentialDocument document) {
        return AiCredentialDTO.builder()
                .id(document.getId())
                .providerId(document.getProviderId())
                .label(document.getLabel())
                .maskedKey(document.getMaskedKey())
                .keyPreview(document.getKeyPreview())
                .organizationId(document.getOrganizationId())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .expiresAt(document.getExpiresAt())
                .enabled(document.isEnabled())
                .lastValidatedAt(document.getLastValidatedAt())
                .lastTestedAt(document.getLastTestedAt())
                .lastTestSuccess(document.getLastTestSuccess())
                .lastTestMessage(document.getLastTestMessage())
                .metadata(document.getMetadata())
                .build();
    }

    // Credential management methods
    public List<AiCredentialDTO> listCredentials(String providerId) {
        return credentialRepository.findByProviderId(providerId)
                .stream()
                .map(this::toCredentialDTO)
                .collect(Collectors.toList());
    }

    public AiCredentialDTO createCredential(String providerId, AiCredentialRequest request) {
        // Validate provider exists
        providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider não encontrado"));

        validateCredentialRequest(request);

        String encryptedApiKey = cryptoService.encrypt(request.getApiKey());
        String maskedKey = cryptoService.maskApiKey(request.getApiKey());
        String keyPreview = cryptoService.getKeyPreview(request.getApiKey());

        Instant now = Instant.now();
        AiCredentialDocument document = AiCredentialDocument.builder()
                .providerId(providerId)
                .label(request.getLabel())
                .encryptedApiKey(encryptedApiKey)
                .maskedKey(maskedKey)
                .keyPreview(keyPreview)
                .organizationId(request.getOrganizationId())
                .enabled(Optional.ofNullable(request.getEnabled()).orElse(true))
                .metadata(request.getMetadata())
                .createdAt(now)
                .updatedAt(now)
                .build();

        AiCredentialDocument saved = credentialRepository.save(document);
        log.info("AI Credential created: {} for provider {}", saved.getLabel(), providerId);

        return toCredentialDTO(saved);
    }

    public AiCredentialDTO updateCredential(String providerId, String credentialId, AiCredentialRequest request) {
        AiCredentialDocument existing = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new IllegalArgumentException("Credential não encontrado"));

        if (!existing.getProviderId().equals(providerId)) {
            throw new IllegalArgumentException("Credential não pertence ao provider especificado");
        }

        validateCredentialRequest(request);

        String encryptedApiKey = existing.getEncryptedApiKey();
        String maskedKey = existing.getMaskedKey();
        String keyPreview = existing.getKeyPreview();

        // Update API key if provided
        if (request.getApiKey() != null && !request.getApiKey().trim().isEmpty()) {
            encryptedApiKey = cryptoService.encrypt(request.getApiKey());
            maskedKey = cryptoService.maskApiKey(request.getApiKey());
            keyPreview = cryptoService.getKeyPreview(request.getApiKey());
        }

        AiCredentialDocument updated = existing.toBuilder()
                .label(request.getLabel())
                .encryptedApiKey(encryptedApiKey)
                .maskedKey(maskedKey)
                .keyPreview(keyPreview)
                .organizationId(request.getOrganizationId())
                .enabled(Optional.ofNullable(request.getEnabled()).orElse(existing.isEnabled()))
                .metadata(request.getMetadata())
                .updatedAt(Instant.now())
                .build();

        AiCredentialDocument saved = credentialRepository.save(updated);
        log.info("AI Credential updated: {}", saved.getLabel());

        return toCredentialDTO(saved);
    }

    public void deleteCredential(String providerId, String credentialId) {
        AiCredentialDocument credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new IllegalArgumentException("Credential não encontrado"));

        if (!credential.getProviderId().equals(providerId)) {
            throw new IllegalArgumentException("Credential não pertence ao provider especificado");
        }

        credentialRepository.delete(credential);
        log.info("AI Credential deleted: {} for provider {}", credential.getLabel(), providerId);
    }

    public AiCredentialSecretResponse revealCredential(String providerId, String credentialId) {
        AiCredentialDocument credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new IllegalArgumentException("Credential não encontrado"));

        if (!credential.getProviderId().equals(providerId)) {
            throw new IllegalArgumentException("Credential não pertence ao provider especificado");
        }

        String decryptedApiKey = cryptoService.decrypt(credential.getEncryptedApiKey());

        return AiCredentialSecretResponse.builder()
                .providerId(credential.getProviderId())
                .credentialId(credential.getId())
                .label(credential.getLabel())
                .apiKey(decryptedApiKey)
                .organizationId(credential.getOrganizationId())
                .expiresAt(credential.getExpiresAt())
                .metadata(credential.getMetadata())
                .build();
    }

    public List<AiModelConfigDTO> listModels(String providerId) {
        return modelRepository.findByProviderId(providerId)
                .stream()
                .map(this::toModelDTO)
                .collect(Collectors.toList());
    }

    public AiModelConfigDTO createModel(String providerId, AiModelConfigRequest request) {
        providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider não encontrado"));

        validateModelRequest(request);

        Instant now = Instant.now();
        AiModelConfigDocument document = AiModelConfigDocument.builder()
                .providerId(providerId)
                .modelName(request.getModelName())
                .alias(request.getAlias())
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .defaultModel(Optional.ofNullable(request.getDefaultModel()).orElse(false))
                .extras(request.getExtras())
                .available(true) // Assume available on manual creation
                .createdAt(now)
                .updatedAt(now)
                .build();

        AiModelConfigDocument saved = modelRepository.save(document);
        log.info("AI Model created: {} for provider {}", saved.getModelName(), providerId);
        return toModelDTO(saved);
    }

    public AiModelConfigDTO updateModel(String providerId, String modelId, AiModelConfigRequest request) {
        AiModelConfigDocument existing = modelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Modelo não encontrado"));

        if (!existing.getProviderId().equals(providerId)) {
            throw new IllegalArgumentException("Modelo não pertence ao provider especificado");
        }

        validateModelRequest(request);

        AiModelConfigDocument updated = existing.toBuilder()
                .modelName(request.getModelName())
                .alias(request.getAlias())
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .defaultModel(Optional.ofNullable(request.getDefaultModel()).orElse(existing.getDefaultModel()))
                .extras(request.getExtras())
                .updatedAt(Instant.now())
                .build();

        AiModelConfigDocument saved = modelRepository.save(updated);
        log.info("AI Model updated: {}", saved.getModelName());
        return toModelDTO(saved);
    }

    public void deleteModel(String providerId, String modelId) {
        AiModelConfigDocument model = modelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Modelo não encontrado"));

        if (!model.getProviderId().equals(providerId)) {
            throw new IllegalArgumentException("Modelo não pertence ao provider especificado");
        }

        modelRepository.delete(model);
        log.info("AI Model deleted: {} for provider {}", model.getModelName(), providerId);
    }

    private void validateModelRequest(AiModelConfigRequest request) {
        if (request.getModelName() == null || request.getModelName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do modelo é obrigatório");
        }
    }

    public AiTestResult testProvider(String providerId, String credentialId) {
        // Lógica de teste simplificada. Pode ser expandida.
        AiProviderDocument provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider não encontrado"));
        AiCredentialDocument credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new IllegalArgumentException("Credencial não encontrada"));

        boolean success = false;
        String message = "";
        long startTime = System.currentTimeMillis();

        try {
            // Simula uma chamada de API para um endpoint de 'health' ou 'models'
            String testUrl = provider.getBaseUrl() + "/v1/models"; // Exemplo para OpenAI
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(cryptoService.decrypt(credential.getEncryptedApiKey()));

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(testUrl, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                success = true;
                message = "Conexão bem-sucedida. Status: " + response.getStatusCode();
            } else {
                message = "Falha na conexão. Status: " + response.getStatusCode();
            }
        } catch (HttpClientErrorException e) {
            message = "Erro HTTP: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        } catch (Exception e) {
            message = "Erro de conexão: " + e.getMessage();
        }

        long latency = System.currentTimeMillis() - startTime;

        // Atualiza o status do teste na credencial
        AiCredentialDocument updatedCredential = credential.toBuilder()
                .lastTestedAt(Instant.now())
                .lastTestSuccess(success)
                .lastTestMessage(message.substring(0, Math.min(message.length(), 200))) // Limita a mensagem
                .build();
        credentialRepository.save(updatedCredential);

        return AiTestResult.builder()
                .success(success)
                .message(message)
                .latencyMs(latency)
                .testedAt(updatedCredential.getLastTestedAt())
                .build();
    }

    public AiPromptTestResponse testPrompt(String providerId, Map<String, Object> request) {
        long startTime = System.currentTimeMillis();

        String credentialId = (String) request.get("credentialId");
        String systemPrompt = (String) request.get("systemPrompt");
        String userPrompt = (String) request.get("userPrompt");
        String modelName = (String) request.get("modelName");

        try {
            AiProviderDocument provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new IllegalArgumentException("Provider não encontrado"));
            AiCredentialDocument credential = credentialRepository.findById(credentialId)
                    .orElseThrow(() -> new IllegalArgumentException("Credencial não encontrada"));

            // Simulação de chamada real
            String responseText = generatePromptResponse(systemPrompt, userPrompt, provider.getSlug());
            long tokens = estimateTokens(systemPrompt + userPrompt + responseText);
            double cost = calculateCost(provider.getSlug(), tokens);
            long latency = System.currentTimeMillis() - startTime;

            return AiPromptTestResponse.builder()
                    .success(true)
                    .message("Teste executado com sucesso")
                    .response(responseText)
                    .tokens((int) tokens)
                    .cost(cost)
                    .latencyMs((int) latency)
                    .providerId(providerId)
                    .modelName(modelName)
                    .testedAt(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Error testing prompt for provider {}: {}", providerId, e.getMessage());
            return AiPromptTestResponse.builder()
                    .success(false)
                    .message("Erro ao testar prompt: " + e.getMessage())
                    .errorDetails(e.toString())
                    .build();
        }
    }

    private String generatePromptResponse(String systemPrompt, String userPrompt, String providerSlug) {
        String baseResponse = "Esta é uma resposta simulada do provider '" + providerSlug + "'. ";
        if (userPrompt.toLowerCase().contains("olá") || userPrompt.toLowerCase().contains("oi")) {
            return baseResponse + "Olá! Como posso ajudar você hoje?";
        }
        return baseResponse
                + "A sua solicitação foi recebida. Para uma resposta real, a lógica de chamada à API do provedor precisa ser implementada neste serviço.";
    }

    private long estimateTokens(String text) {
        if (text == null)
            return 0;
        return Math.max(1, text.length() / 4);
    }

    private double calculateCost(String providerSlug, long tokens) {
        double costPer1K = switch (providerSlug.toLowerCase()) {
            case "openai" -> 0.0015; // gpt-3.5-turbo approx
            case "anthropic" -> 0.008; // claude-3-haiku approx
            case "groq" -> 0.00027; // llama3-8b approx
            case "google" -> 0.001; // gemini-pro approx
            default -> 0.0; // ollama is free
        };
        return (tokens * costPer1K) / 1000.0;
    }
}