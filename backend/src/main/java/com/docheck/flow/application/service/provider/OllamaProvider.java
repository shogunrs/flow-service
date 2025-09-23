package com.docheck.flow.application.service.provider;

import com.docheck.flow.api.dto.ChatRequest;
import com.docheck.flow.api.dto.ChatResponse;
import com.docheck.flow.application.service.security.EncryptionService;
import com.docheck.flow.domain.model.AiProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("ollama")
@RequiredArgsConstructor
public class OllamaProvider implements ChatProvider {

    // Ollama may not need an API key, but we inject the service for consistency.
    private final EncryptionService encryptionService;

    @Override
    public ChatResponse getChatResponse(ChatRequest request, AiProvider provider) {
        // Ollama doesn't typically use an API key, but we can decrypt if one is provided.
        String apiKey = encryptionService.decrypt(provider.getApiKey());

        String baseUrl = provider.getBaseUrl();
        String model = request.getModel();
        String prompt = request.getPrompt();

        // Ollama API uses a specific request structure
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(message));
        requestBody.put("stream", false); // We want a single response

        String fullUrl = baseUrl;
        if (!fullUrl.endsWith("/")) {
            fullUrl += "/";
        }
        fullUrl += "api/chat";

        WebClient.Builder webClientBuilder = WebClient.builder();

        // Add Authorization header only if an API key is present
        if (apiKey != null && !apiKey.isEmpty()) {
            webClientBuilder.defaultHeader("Authorization", "Bearer " + apiKey);
        }

        WebClient webClient = webClientBuilder.build();

        try {
            String jsonResponse = webClient.post()
                .uri(fullUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            String content = extractContentFromJson(jsonResponse);

            ChatResponse response = new ChatResponse();
            response.setContent(content);
            return response;

        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            throw new RuntimeException("API call failed with status " + e.getStatusCode() + " and body " + errorBody, e);
        } catch (Exception e) {
            throw new RuntimeException("Error during API call: " + e.getMessage(), e);
        }
    }

    private String extractContentFromJson(String jsonResponse) {
        // Ollama API has a different response structure
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode messageNode = rootNode.path("message");
            return messageNode.path("content").asText();
        } catch (IOException e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
            return "Error parsing response";
        }
    }
}
