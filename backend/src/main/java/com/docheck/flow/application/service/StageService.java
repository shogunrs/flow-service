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
        // Busca estágios existentes para preservar IDs quando possível
        List<Stage> existing = repo.findByProcessKeyOrderByOrder(processKey);
        Map<String, Stage> existingById = new java.util.HashMap<>();
        Map<String, Stage> existingByTitle = new java.util.HashMap<>();
        
        for (Stage e : existing) {
            if (e.getId() != null) {
                existingById.put(e.getId(), e);
            }
            if (e.getTitle() != null) {
                existingByTitle.put(e.getTitle(), e);
            }
        }
        
        List<Stage> toSave = new ArrayList<>();
        List<String> idsToKeep = new ArrayList<>();
        int i = 0;
        
        for (Stage s : incoming) {
            Stage existingStage = null;
            
            // Tenta encontrar estágio existente para preservar ID
            if (s.getId() != null && !s.getId().isBlank() && existingById.containsKey(s.getId())) {
                existingStage = existingById.get(s.getId());
            } else if (s.getTitle() != null && !s.getTitle().isBlank() && existingByTitle.containsKey(s.getTitle())) {
                existingStage = existingByTitle.get(s.getTitle());
            }
            // REMOVIDO: fallback por posição que causava IDs fantasmas
            
            if (existingStage != null) {
                // Atualiza estágio existente preservando ID
                existingStage.setTitle(s.getTitle());
                existingStage.setSlaDays(s.getSlaDays());
                existingStage.setColor(s.getColor());
                existingStage.setOrder(i);
                existingStage.setDefaultStatus(s.getDefaultStatus());
                existingStage.setUpdatedAt(Instant.now());
                toSave.add(existingStage);
                idsToKeep.add(existingStage.getId());
            } else {
                // Cria novo estágio apenas quando necessário
                Stage ns = new Stage(null, processKey,
                        s.getTitle(), s.getSlaDays(), s.getColor(), s.getDefaultStatus(), i,
                        Instant.now(), Instant.now());
                toSave.add(ns);
            }
            i++;
        }
        
        // Remove apenas estágios que não estão sendo preservados
        for (Stage e : existing) {
            if (!idsToKeep.contains(e.getId())) {
                repo.deleteById(e.getId());
            }
        }
        
        List<Stage> saved = repo.saveAll(toSave);
        publisher.publish("stage.bulk.updated", Map.of("processKey", processKey, "count", saved.size()));
        return saved;
    }
}
