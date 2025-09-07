package com.docheck.flow.application.service;

import com.docheck.flow.application.port.EventPublisher;
import com.docheck.flow.application.port.ProcessRepository;
import com.docheck.flow.domain.model.Process;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ProcessService {
    private final ProcessRepository repo;
    private final EventPublisher publisher;

    public ProcessService(ProcessRepository repo, EventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    public List<Process> list() { return repo.findAll(); }

    public Optional<Process> get(String key) { return repo.findByKey(key); }

    @Transactional
    public Process create(String key, String name) {
        if (repo.existsByKey(key)) throw new IllegalArgumentException("process key already exists");
        Process p = new Process(null, key, name, true, Instant.now(), Instant.now());
        Process saved = repo.save(p);
        publisher.publish("process.created", Map.of("key", key, "name", name));
        return saved;
    }

    @Transactional
    public Process updateName(String key, String name) {
        Process p = repo.findByKey(key).orElseThrow();
        p.setName(name);
        p.setUpdatedAt(Instant.now());
        Process saved = repo.save(p);
        publisher.publish("process.updated", Map.of("key", key, "name", name));
        return saved;
    }

    @Transactional
    public void delete(String key) {
        repo.deleteByKey(key);
        publisher.publish("process.deleted", Map.of("key", key));
    }
}

