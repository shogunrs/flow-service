package com.docheck.flow.api;

import com.docheck.flow.api.dto.*;
import com.docheck.flow.api.dto.AiPromptTestResponse;
import com.docheck.flow.application.service.AiProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/providers")
public class AiProviderController {

    private final AiProviderService service;

    public AiProviderController(AiProviderService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> listProviders() {
        List<AiProviderDTO> providers = service.listProviders();
        return ResponseEntity.ok(Map.of("success", true, "data", providers));
    }

    @PostMapping
    public ResponseEntity<?> createProvider(@RequestBody AiProviderRequest request) {
        try {
            AiProviderDTO provider = service.createProvider(request);
            return ResponseEntity.ok(Map.of("success", true, "data", provider));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{providerId}")
    public ResponseEntity<?> updateProvider(@PathVariable("providerId") String providerId,
            @RequestBody AiProviderRequest request) {
        try {
            AiProviderDTO provider = service.updateProvider(providerId, request);
            return ResponseEntity.ok(Map.of("success", true, "data", provider));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{providerId}")
    public ResponseEntity<?> deleteProvider(@PathVariable("providerId") String providerId) {
        try {
            service.deleteProvider(providerId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Provider deletado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{providerId}/credentials")
    public ResponseEntity<?> listCredentials(@PathVariable("providerId") String providerId) {
        List<AiCredentialDTO> credentials = service.listCredentials(providerId);
        return ResponseEntity.ok(Map.of("success", true, "data", credentials));
    }

    @PostMapping("/{providerId}/credentials")
    public ResponseEntity<?> createCredential(@PathVariable("providerId") String providerId,
            @RequestBody AiCredentialRequest request) {
        try {
            AiCredentialDTO credential = service.createCredential(providerId, request);
            return ResponseEntity.ok(Map.of("success", true, "data", credential));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{providerId}/credentials/{credentialId}")
    public ResponseEntity<?> updateCredential(@PathVariable("providerId") String providerId,
            @PathVariable("credentialId") String credentialId,
            @RequestBody AiCredentialRequest request) {
        try {
            AiCredentialDTO credential = service.updateCredential(providerId, credentialId, request);
            return ResponseEntity.ok(Map.of("success", true, "data", credential));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{providerId}/credentials/{credentialId}")
    public ResponseEntity<?> deleteCredential(@PathVariable("providerId") String providerId,
            @PathVariable("credentialId") String credentialId) {
        try {
            service.deleteCredential(providerId, credentialId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Credential deletado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{providerId}/models")
    public ResponseEntity<?> listModels(@PathVariable("providerId") String providerId) {
        List<AiModelConfigDTO> models = service.listModels(providerId);
        return ResponseEntity.ok(Map.of("success", true, "data", models));
    }

    @PostMapping("/{providerId}/models")
    public ResponseEntity<?> createModel(@PathVariable("providerId") String providerId,
            @RequestBody AiModelConfigRequest request) {
        try {
            AiModelConfigDTO model = service.createModel(providerId, request);
            return ResponseEntity.ok(Map.of("success", true, "data", model));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{providerId}/models/{modelId}")
    public ResponseEntity<?> updateModel(@PathVariable("providerId") String providerId,
            @PathVariable("modelId") String modelId,
            @RequestBody AiModelConfigRequest request) {
        try {
            AiModelConfigDTO model = service.updateModel(providerId, modelId, request);
            return ResponseEntity.ok(Map.of("success", true, "data", model));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{providerId}/models/{modelId}")
    public ResponseEntity<?> deleteModel(@PathVariable("providerId") String providerId,
            @PathVariable("modelId") String modelId) {
        try {
            service.deleteModel(providerId, modelId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Model deletado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{providerId}/auto-detect-models")
    public ResponseEntity<?> autoDetectModels(@PathVariable("providerId") String providerId) {
        try {
            List<AiModelConfigDTO> models = service.autoDetectModels(providerId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", models,
                    "message", String.format("Detectados %d modelos", models.size())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/quick-setup/ollama")
    public ResponseEntity<?> quickSetupOllama() {
        try {
            Map<String, Object> result = service.quickSetupOllama();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result.get("data"),
                    "message", result.get("message"),
                    "alreadyExists", result.get("alreadyExists")));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{providerId}/test-prompt")
    public ResponseEntity<?> testPrompt(@PathVariable("providerId") String providerId,
            @RequestBody Map<String, Object> request) {
        // Este endpoint agora delega a lógica para o serviço, que pode ser expandido
        // para fazer chamadas HTTP reais.
        try {
            AiPromptTestResponse response = service.testPrompt(providerId, request);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (Exception e) {
            AiPromptTestResponse errorResponse = new AiPromptTestResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Erro ao testar prompt: " + e.getMessage());
            errorResponse.setErrorDetails(e.toString());
            return ResponseEntity.badRequest().body(Map.of("success", false, "data", errorResponse));
        }
    }

    @PostMapping("/{providerId}/test")
    public ResponseEntity<?> testProvider(@PathVariable("providerId") String providerId,
            @RequestBody Map<String, String> request) {
        try {
            AiTestResult result = service.testProvider(providerId, request.get("credentialId"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Erro ao testar provider: " + e.getMessage()));
        }
    }
}