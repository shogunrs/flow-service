package com.docheck.flow.api;

import com.docheck.flow.api.dto.ChatRequest;
import com.docheck.flow.api.dto.ChatResponse;
import com.docheck.flow.api.dto.AiProviderResponse;
import com.docheck.flow.api.dto.AiProviderUpdateRequest;
import com.docheck.flow.application.service.AiProviderService;
import com.docheck.flow.domain.model.AiProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ai-providers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // WARNING: For development only. Configure properly for production.
public class AiProviderController {

    private final AiProviderService aiProviderService;

    @PutMapping("/{id}")
    public ResponseEntity<AiProviderResponse> updateProvider(
            @PathVariable String id,
            @RequestBody AiProviderUpdateRequest request) {

        // In a real app, the name would likely be part of the request or handled
        // differently
        String name = Character.toUpperCase(id.charAt(0)) + id.substring(1);

        AiProvider updatedProvider = aiProviderService.updateProvider(id, name, request);
        return ResponseEntity.ok(AiProviderResponse.fromEntity(updatedProvider));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiProviderResponse> getProvider(@PathVariable String id) {
        return aiProviderService.getProvider(id)
                .map(provider -> ResponseEntity.ok(AiProviderResponse.fromEntity(provider)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AiProviderResponse>> getAllProviders() {
        List<AiProviderResponse> responses = aiProviderService.getAllProviders().stream()
                .map(AiProviderResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> getChatResponse(@RequestBody ChatRequest request) {
        // This will call a new method in AiProviderService to handle the chat logic
        ChatResponse response = aiProviderService.getChatResponse(request);
        return ResponseEntity.ok(response);
    }
}
