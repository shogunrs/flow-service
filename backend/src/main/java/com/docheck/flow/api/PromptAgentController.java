package com.docheck.flow.api;

import com.docheck.flow.api.dto.CreateOrUpdatePromptAgentRequest;
import com.docheck.flow.api.dto.PromptAgentDto;
import com.docheck.flow.application.service.PromptAgentService;
import com.docheck.flow.domain.model.PromptAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/prompt-agents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // WARNING: For development only
public class PromptAgentController {

    private final PromptAgentService promptAgentService;

    @GetMapping
    public ResponseEntity<List<PromptAgentDto>> getAllAgents() {
        List<PromptAgentDto> agents = promptAgentService.getAllAgents().stream()
                .map(PromptAgentDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(agents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptAgentDto> getAgentById(@PathVariable String id) {
        return promptAgentService.getAgentById(id)
                .map(agent -> ResponseEntity.ok(PromptAgentDto.fromEntity(agent)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PromptAgentDto> createAgent(@RequestBody CreateOrUpdatePromptAgentRequest request) {
        PromptAgent createdAgent = promptAgentService.createAgent(request);
        return ResponseEntity.ok(PromptAgentDto.fromEntity(createdAgent));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromptAgentDto> updateAgent(@PathVariable String id, @RequestBody CreateOrUpdatePromptAgentRequest request) {
        return promptAgentService.updateAgent(id, request)
                .map(agent -> ResponseEntity.ok(PromptAgentDto.fromEntity(agent)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgent(@PathVariable String id) {
        promptAgentService.deleteAgent(id);
        return ResponseEntity.noContent().build();
    }
}
