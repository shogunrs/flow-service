package com.docheck.flow.application.service;

import com.docheck.flow.api.dto.CreateOrUpdatePromptAgentRequest;
import com.docheck.flow.domain.model.PromptAgent;
import com.docheck.flow.infrastructure.mongo.PromptAgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromptAgentService {

    private final PromptAgentRepository promptAgentRepository;

    public List<PromptAgent> getAllAgents() {
        return promptAgentRepository.findAll();
    }

    public Optional<PromptAgent> getAgentById(String id) {
        return promptAgentRepository.findById(id);
    }

    public PromptAgent createAgent(CreateOrUpdatePromptAgentRequest request) {
        PromptAgent agent = new PromptAgent();
        agent.setName(request.getName());
        agent.setPrompt(request.getPrompt());
        return promptAgentRepository.save(agent);
    }

    public Optional<PromptAgent> updateAgent(String id, CreateOrUpdatePromptAgentRequest request) {
        return promptAgentRepository.findById(id).map(agent -> {
            agent.setName(request.getName());
            agent.setPrompt(request.getPrompt());
            return promptAgentRepository.save(agent);
        });
    }

    public void deleteAgent(String id) {
        promptAgentRepository.deleteById(id);
    }
}
