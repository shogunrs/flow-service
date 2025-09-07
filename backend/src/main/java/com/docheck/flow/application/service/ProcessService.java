package com.docheck.flow.application.service;

import com.docheck.flow.application.port.EventPublisher;
import com.docheck.flow.application.port.ProcessRepository;
import com.docheck.flow.application.port.StageRepository;
import com.docheck.flow.domain.model.Process;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ProcessService {
    private final ProcessRepository repo;
    private final StageRepository stageRepo;
    private final EventPublisher publisher;

    public ProcessService(ProcessRepository repo, StageRepository stageRepo, EventPublisher publisher) {
        this.repo = repo;
        this.stageRepo = stageRepo;
        this.publisher = publisher;
    }

    public List<Process> list() { return repo.findAll(); }

    public Optional<Process> get(String externalId) { return repo.findByExternalId(externalId); }

    @Transactional
    public Process create(String externalId, String name) {
        String ex = (externalId == null || externalId.isBlank()) ? java.util.UUID.randomUUID().toString() : externalId;
        if (repo.existsByExternalId(ex)) throw new IllegalArgumentException("process id already exists");
        Process p = new Process(null, ex, name, true, Instant.now(), Instant.now());
        Process saved = repo.save(p);
        publisher.publish("process.created", Map.of("id", ex, "name", name));
        return saved;
    }

    @Transactional
    public Process updateName(String externalId, String name) {
        Process p = repo.findByExternalId(externalId).orElseThrow();
        p.setName(name);
        p.setUpdatedAt(Instant.now());
        Process saved = repo.save(p);
        publisher.publish("process.updated", Map.of("id", externalId, "name", name));
        return saved;
    }

    @Transactional
    public void delete(String externalId) {
        // Remove etapas associadas antes de deletar o processo (limpa lixo)
        try { stageRepo.deleteByProcessKey(externalId); } catch (Exception ignored) {}
        repo.deleteByExternalId(externalId);
        publisher.publish("process.deleted", Map.of("id", externalId));
    }
}
