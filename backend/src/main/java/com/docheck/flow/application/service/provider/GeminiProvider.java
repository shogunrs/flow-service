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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("gemini")
@RequiredArgsConstructor
public class GeminiProvider implements ChatProvider {

    private final EncryptionService encryptionService;

    @Override
    public ChatResponse getChatResponse(ChatRequest request, AiProvider provider) {
        String decryptedApiKey = encryptionService.decrypt(provider.getApiKey());

        String baseUrl = provider.getBaseUrl();
        String model = request.getModel();
        String prompt = request.getPrompt();

        Map<String, Object> contentPart = new HashMap<>();
        contentPart.put("text", prompt);

        Map<String, Object> contents = new HashMap<>();
        contents.put("parts", List.of(contentPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(contents));

        String sanitizedModel = model.toLowerCase().replace(' ', '-');

        // Construct the URL using a builder to ensure proper encoding.
        // The base URL already contains the version, so we only add the specific endpoint path.
        String fullUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/models/{model}:generateContent")
            .queryParam("key", decryptedApiKey)
            .buildAndExpand(sanitizedModel)
            .toUriString();

        WebClient webClient = WebClient.builder().build();

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
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
            return "No content found in response";
        } catch (IOException e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
            return "Error parsing response";
        }
    }
}
