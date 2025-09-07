package com.docheck.flow.application.service;

import com.docheck.flow.application.port.EventPublisher;
import com.docheck.flow.application.port.StageRepository;
import com.docheck.flow.domain.model.Stage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StageService {
    private final StageRepository repo;
    private final EventPublisher publisher;

    public StageService(StageRepository repo, EventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    public List<Stage> listByProcess(String processKey) {
        return repo.findByProcessKeyOrderByOrder(processKey);
    }

    @Transactional
    public List<Stage> replaceForProcess(String processKey, List<Stage> incoming) {
        repo.deleteByProcessKey(processKey);
        List<Stage> toSave = new ArrayList<>();
        int i = 0;
        for (Stage s : incoming) {
            Stage ns = new Stage(null, processKey,
                    s.getTitle(), s.getSlaDays(), s.getColor(), i++,
                    Instant.now(), Instant.now());
            toSave.add(ns);
        }
        List<Stage> saved = repo.saveAll(toSave);
        publisher.publish("stage.bulk.updated", Map.of("processKey", processKey, "count", saved.size()));
        return saved;
    }
}

