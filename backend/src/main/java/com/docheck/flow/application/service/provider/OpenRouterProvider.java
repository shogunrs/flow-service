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

@Component("openrouter")
@RequiredArgsConstructor
public class OpenRouterProvider implements ChatProvider {

    private final EncryptionService encryptionService;

    @Override
    public ChatResponse getChatResponse(ChatRequest request, AiProvider provider) {
        String decryptedApiKey = encryptionService.decrypt(provider.getApiKey());

        String baseUrl = provider.getBaseUrl();
        String model = request.getModel();
        String prompt = request.getPrompt();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        requestBody.put("temperature", 0.7);

        String fullUrl = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";

        WebClient webClient = WebClient.builder().build();

        try {
            String jsonResponse = webClient.post()
                .uri(fullUrl)
                .header("Authorization", "Bearer " + decryptedApiKey)
                .header("HTTP-Referer", "https://flow-service.docheck.com.br/") // Recommended by OpenRouter
                .header("X-Title", "Flow Service") // Recommended by OpenRouter
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
            JsonNode choices = rootNode.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.path("message");
                return message.path("content").asText();
            }
            return "No content found in response";
        } catch (IOException e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
            return "Error parsing response";
        }
    }
}
